#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number:
 *
 * Group members
 * Member 1
 * Member 2
 * Member 3
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 1

const int num_segments = 20;

const int num_iterations = 40;
const int count_every = 2;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input()
{
#if DEBUG
  return 1;
#elif
  return rand() % max_in_per_sec;
#endif
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment()
{
#if DEBUG
  return 1;
#elif
  return rand() < alpha ? 1 : 0;
#endif
}

int main(int argc, char **argv)
{
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  // TODO: define and init variables

  int num_of_segments = num_segments / num_procs; // we assume num_segments as a multiple of num_procs
  int *cars_per_segment = malloc(sizeof(int) * num_of_segments);
  int leaving_cars = 0;
  int arriving_cars = 0;

  for (int i = 0; i < num_of_segments; i++)
  {
    cars_per_segment[i] = 0;
  }

  // Simulate for num_iterations iterations
  for (int it = 0; it < num_iterations; ++it)
  {
    leaving_cars = 0;
    for (int i = 0; i < cars_per_segment[num_of_segments - 1]; i++)
    {
      if (move_next_segment() == 1)
      {
        cars_per_segment[num_of_segments - 1] = cars_per_segment[num_of_segments - 1] - 1;
        leaving_cars++;
      }
    }

    // move inputs in between the same process
    for (int seg_iterator = num_of_segments - 1; seg_iterator >= 0; seg_iterator--)
    {
      for (int i = 0; i < cars_per_segment[seg_iterator]; i++)
      {
        if (move_next_segment() == 1)
        {
          cars_per_segment[seg_iterator] = cars_per_segment[seg_iterator] - 1;
          cars_per_segment[seg_iterator + 1] = cars_per_segment[seg_iterator + 1] + 1;
        }
      }
    }
    // Move cars across segments
    // New cars may enter in the first segment
    // Cars may exit from the last segment
    if (rank != num_procs - 1)
    {
      // send the leaving cars to the next segment
      MPI_Send(&leaving_cars, 1, MPI_INT, rank + 1, 0, MPI_COMM_WORLD);
    }

    arriving_cars = 0;
    if (rank == 0)
    {
      // add input to the first segment
      arriving_cars = create_random_input();
    }
    else
    {
      // receive the arriving cars
      MPI_Recv(&arriving_cars, 1, MPI_INT, rank - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }
    cars_per_segment[0] = cars_per_segment[0] + arriving_cars;

    // When needed, compute the overall sum
    if (it % count_every == 0)
    {
      int global_sum = 0;
      int local_sum = 0;

      for (int i = 0; i < num_of_segments; i++)
      {
        local_sum = local_sum + cars_per_segment[i];
      }

      // TODO compute global sum
      MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0,
                 MPI_COMM_WORLD);

      if (rank == 0)
      {
        printf("Iteration: %d, sum: %d\n", it, global_sum);
      }
    }

    MPI_Barrier(MPI_COMM_WORLD);
  }

  // TODO deallocate dynamic variables, if needed

  free(cars_per_segment);

  MPI_Finalize();
}
