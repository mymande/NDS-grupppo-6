#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>
#include <math.h>

/*
 * Group number: 06
 *
 * Group members
 *  - Andrea Carbonetti
 *  - Federico Mandelli
 *  - Pasquale Scalise
 */

const float min = 0;
const float max = 1000;
const float len = max - min;
const int num_ants = 8 * 1000 * 1000;
const int num_food_sources = 10;
const int num_iterations = 500;

float compute_center(float *ants, int array_size)
{
  float sum = 0;
  for (int i = 0; i < array_size; i++)
  {
    sum += ants[i];
  }
  return sum / array_size;
}

float distance_to_point(float ant_position, float point_position)
{
  return point_position - ant_position;
}

float find_closest_food(float *food, float ant_position)
{
  float closest_food = 0;
  float closest_food_distance = max;
  for (int f = 0; f < num_food_sources; f++)
  {
    if (abs(ant_position - food[f]) < closest_food_distance)
    {
      closest_food_distance = abs(ant_position - food[f]);
      closest_food = food[f];
    }
  }

  return closest_food;
}

float random_position()
{
  return (float)rand() / (float)(RAND_MAX / (max - min)) + min;
}

/*
 * Process 0 invokes this function to initialize food sources.
 */
void init_food_sources(float *food_sources)
{
  for (int i = 0; i < num_food_sources; i++)
  {
    food_sources[i] = random_position();
  }
}

/*
 * Process 0 invokes this function to initialize the position of ants.
 */
void init_ants(float *ants)
{
  for (int i = 0; i < num_ants; i++)
  {
    ants[i] = random_position();
  }
}

int main()
{
  MPI_Init(NULL, NULL);

  int rank;
  int num_procs;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(rank);

  // Allocate space in each process for food sources and ants
  int ants_per_process = num_ants / num_procs;

  float *food_positions = malloc(sizeof(float) * num_food_sources);
  float *ants_positions = malloc(sizeof(float) * ants_per_process);

  // Process 0 initializes food sources and ants
  float *total_ants;
  if (rank == 0)
  {
    // we allocate total_ants used only by process 0
    total_ants = malloc(sizeof(float) * num_ants);
    init_ants(total_ants);
    init_food_sources(food_positions);
  }

  // Process 0 distribute food sources and ants
  MPI_Scatter(total_ants, ants_per_process, MPI_FLOAT, ants_positions, ants_per_process, MPI_FLOAT, 0, MPI_COMM_WORLD);
  MPI_Bcast(food_positions, num_food_sources, MPI_FLOAT, 0, MPI_COMM_WORLD);

  // Iterative simulation
  float center = 0;
  if (rank == 0)
  {
    center = compute_center(total_ants, num_ants);
  }

  for (int iter = 0; iter < num_iterations; iter++)
  {
    float sum = 0;

    MPI_Bcast(&center, 1, MPI_FLOAT, 0, MPI_COMM_WORLD);

    for (int i = 0; i < ants_per_process; i++)
    {
      // Force is negative if it attract to the left, otherwise it is positive
      float attraction_to_center = 0.012 * distance_to_point(ants_positions[i], center);

      float closest_food = find_closest_food(food_positions, ants_positions[i]);

      float attraction_to_food = 0.01 * distance_to_point(ants_positions[i], closest_food);

      // Update position
      ants_positions[i] = ants_positions[i] + attraction_to_center + attraction_to_food;

      if (ants_positions[i] < min)
      {
        ants_positions[i] = min;
      }
      else if (ants_positions[i] > max)
      {
        ants_positions[i] = max;
      }
      sum = sum + ants_positions[i];
    }
    float local_avg = (sum / ants_per_process) / num_procs;

    MPI_Reduce(&local_avg, &center, 1, MPI_FLOAT, MPI_SUM, 0, MPI_COMM_WORLD);

    if (rank == 0)
    {
      printf("Iteration: %d - Average position: %f\n", iter, center);
    }
  }

  // Free memory
  free(food_positions);
  free(ants_positions);

  if (rank == 0)
  {
    // we de-allocate total_ants only used by process 0
    free(total_ants);
  }
  MPI_Finalize();
  return 0;
}
