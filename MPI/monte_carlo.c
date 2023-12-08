#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

const int num_iter_per_proc = 10 * 1000 * 1000;

int main()
{
  MPI_Init(NULL, NULL);

  int rank = 0;
  int num_procs;
  float sum = 0;
  MPI_Status status;

  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(time(NULL) + rank);

  // TODO
  for (int i = 0; i < num_iter_per_proc; i++)
  {
    float pointX = ((double)rand() / (RAND_MAX));
    float pointY = ((double)rand() / (RAND_MAX));

    if (pointX * pointX + pointY * pointY < 1)
    {
      sum++;
    }
  }
  if (rank == 0)
  {
    for (int i = 0; i < num_procs - 1; i++)
    {
      float temp;
      MPI_Recv(&temp, 1, MPI_FLOAT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
      sum += temp;
    }

    double pi = (4.0 * sum) / (num_iter_per_proc * num_procs);
    printf("Pi = %f\n", pi);
  }
  else
  {
    MPI_Send(&sum, 1, MPI_FLOAT, 0, 0, MPI_COMM_WORLD);
  }

  MPI_Finalize();
  return 0;
}