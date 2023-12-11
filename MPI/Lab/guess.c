#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

int rank;
int num_procs;
int tag = 0;

const int num_rounds = 1000;

const int min_num = 1;
const int max_num = 1000;

// Array, one element per process
// The leader board, instantiated and used in process 0
int *leaderboard = NULL;

// Array, one element per process
// The array of number selected in the current round
int *selected_numbers = NULL;

// The leader for the current round
int leader = 0;

// Allocate dynamic variables
void allocate_vars()
{
  // TODO
  leaderboard = malloc(sizeof(int) * num_procs);
  for (int i = 0; i < num_procs; i++)
  {
    leaderboard[i] = 0;
  }
  selected_numbers = malloc(sizeof(int) * num_procs);
  for (int i = 0; i < num_procs; i++)
  {
    selected_numbers[i] = 0;
  }
}

// Deallocate dynamic variables
void free_vars()
{
  // TODO
  free(leaderboard);
  free(selected_numbers);
}

// Select a random number between min_num and max_num
int select_number()
{
  return min_num + rand() % (max_num - min_num + 1);
}

// Function used to communicate the selected number to the leader
void send_num_to_leader(int num)
{
  MPI_Gather(&num, 1, MPI_INT, selected_numbers, 1, MPI_INT, leader, MPI_COMM_WORLD);

  // TODO
}

// Compute the winner (-1 if there is no winner)
// Function invoked by the leader only
int compute_winner(int number_to_guess)
{
  int minDistance = 1001;
  int winner = -1;
  int counter = 0;
  for (int i = 0; i < num_procs; i++)
  {
    if (abs(selected_numbers[i] - number_to_guess) < minDistance)
    {
      counter = 0;
      minDistance = abs(selected_numbers[i] - number_to_guess);
      winner = i;
    }
    else if (abs(selected_numbers[i] - number_to_guess) == minDistance)
    {
      counter++;
    }
  }
  printf("leader %d, winner%d, num to guess%d\n", leader, winner, number_to_guess);
  if (counter != 0)
  {
    return -1;
  }
  // TODO
  return winner;
}

// Function used to communicate the winner to everybody
void send_winner(int *winner)
{
  if (*winner == -1)
  {
    *winner = leader;
  }
  MPI_Bcast(winner, 1, MPI_INT, leader, MPI_COMM_WORLD);
  // TODO
}

// Update leader
void update_leader(int winner)
{
  leader = winner;
  // TODO
}

// Update leaderboard (invoked by process 0 only)
void update_leaderboard(int winner)
{
  // TODO
  leaderboard[winner]++;
}

// Print the leaderboard
void print_leaderboard(int round, int winner)
{
  printf("\n* Round %d *\n", round);
  printf("Winner: %d\n", winner);
  printf("Leaderboard\n");
  for (int i = 0; i < num_procs; i++)
  {
    printf("P%d:\t%d\n", i, leaderboard[i]);
  }
}

int main(int argc, char **argv)
{
  MPI_Init(NULL, NULL);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
  srand(time(NULL) + rank);

  allocate_vars();

  for (int round = 0; round < num_rounds; round++)
  {
    int selected_number = select_number();
    send_num_to_leader(selected_number);

    int winner;
    if (rank == leader)
    {
      int num_to_guess = select_number();
      winner = compute_winner(num_to_guess);
    }
    send_winner(&winner);
    update_leader(winner);

    if (rank == 0)
    {
      update_leaderboard(winner);
      print_leaderboard(round, winner);
    }
  }

  MPI_Barrier(MPI_COMM_WORLD);
  free_vars();

  MPI_Finalize();
}