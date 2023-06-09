/* Consisting out of Point + Direction -> Next point. A protein folding is generated and by walking down these points we evaluate its fitness = energy / overlaps */
package Base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


// P1
// ================================================================================================================
public class Folding {

  /* Attributes */
  /* Consists out of the directions of the points in their given sequence */
  private final List<Direction> directions;
  /* Counter for overlaps */
  private int overlaps = 0;
  /* Counter for energy */
  private int energy = 0;
  /* Counter for fitness of folding */
  private float fitness = -1;

  /* Getters */
  public List<Direction> getDirections()  {return this.directions;}
  public int getOverlaps()                {return this.overlaps;}
  public int getEnergy()                  {return this.energy;}
  public float getFitness()               {return this.fitness;}


  /* Constructor */
  public Folding(List<Direction> directions) {
    this.directions = new LinkedList<>();
    this.directions.addAll(directions);
  }


  /* Functions */
  /* Depending on the directions and seq given, calculate overlaps and energy => fitness */
  public float analyzeFolding(String sequence) {

    /* Sequence and directions have to match in order for successful folding generation */
    if (this.directions.size() + 1 != sequence.length()) return -1;

    /* If theres already a fitness we evaluated already */
    if (this.fitness >= 0) return fitness;


    // Start at 0/0 and in function iterate to according position
    Point currentPosition = new Point(0, 0);
    // Standard move is move up by 0/1 -> will be altered by changeDeltaVector
    Point currDelta = new Point(0, 1);



    // MOVE THROUGH ALL THE POINTS IN FOLDING and calculate values in following points in folding
    for (int i = 0; i < this.directions.size(); i++) {
      // Start at 0/0
      Point iteratePosition = new Point(currentPosition.x, currentPosition.y);
      // Change delta vector
      // Also needs to be backed up to change from here with next iteration over again(!!!!)
      Point backupDelta = new Point(currDelta.x, currDelta.y);

      // FOR ALL FUTURE POSITIONS CHECK IF ONE IS AN OVERLAP AND OR HYDROPHOBIC CONNECTION
      for (int j = i; j < this.directions.size(); j++) {
        // calculate next delta vector (0/1, 0/-1, 1/0, 1/1)
        changeDeltaVector(backupDelta, this.directions.get(j));
        // depending on the current delta change next position
        moveToNextPositions(iteratePosition, backupDelta);

        // For this new position check for overlaps and energy
        if (j > i) {
          if (currentPosition.equals(iteratePosition)) overlaps++;
          if (sequence.charAt(i) == '1' && sequence.charAt(j + 1) == '1' && isNeighbour(currentPosition, iteratePosition)) energy++;
        }
      }

      // Move on to next point with the previous delta vector and check all its following ones
      changeDeltaVector(currDelta, this.directions.get(i));
      moveToNextPositions(currentPosition, currDelta);
    }

    this.fitness = (float)energy / (float)(overlaps+1);
    return this.fitness;
  }

  // Checks the current delta direction (0/1, 0/-1, 1/0, 1/1) together with the next direction to go to and changes the vector to add to the prev point
  private void changeDeltaVector(Point currentDelta, Direction direction) {
    // Calculate the delta for the next position
    int x = currentDelta.x;
    int y = currentDelta.y;

    switch (direction) {
      case Left: // Rotate Vector left
        x = currentDelta.y * (-1);
        y = currentDelta.x;
        break;
      case Straight:
        break;
      case Right: // Rotate Vector right
        x = currentDelta.y;
        y = currentDelta.x * (-1);
        break;
    }

    currentDelta.x = x;
    currentDelta.y = y;
  }


  // Sets new position of next amino acid as well as save the current Delta if it changed with another direction
  private void moveToNextPositions(Point iteratePos, Point currentDelta) {
    // Add the x and y coords to the position to move it to its next position
    iteratePos.x += currentDelta.x;
    iteratePos.y += currentDelta.y;

  }

  // Check if the distance between two points, depending on their x and y position, is adjacent
  private boolean isNeighbour(Point pos2, Point pos1) {
    // Calculate positive hypothenuses around a square, with a neighbours straight = 1, diagonal ~= 1.4, overlapping ~= 0
    double dist = Math.sqrt(Math.pow(pos1.x - pos2.x, 2) + Math.pow(pos1.y - pos2.y, 2));
    return dist == 1;
  }



  /* IMAGING FUNCTIONS */
  /*======================================*/
  // check if overlaps exist at a given position by traversing all other points
  public int getOverlapsAtPosition(int index) {
    int o = 0;
    Point position = getPosition(index);
    for (int i = 0; i < directions.size(); i++) {
      if (position.equals(getPosition(i)) && index != i)
        o++;
    }

    return o;
  }

  // Get position in x / y coords by traversing to the point
  public Point getPosition(int index) {
    Point currentDirectionDelta = new Point(0, 1);
    Point position = new Point(0, 0);

    for (int i = 0; i < index; i++) {
      // calculate next delta vector (0/1, 0/-1, 1/0, 1/1)
      changeDeltaVector(currentDirectionDelta, this.directions.get(i));
      moveToNextPositions(position, currentDirectionDelta);
    }

    return position;
  }

  // Get minimum coordinates point in folding for x and y
  public Point getMinValue() {

    Point currentDirectionDelta = new Point(0, 1);
    Point position = new Point(0, 0);

    Point minValue = new Point(0, 0);

    for (Direction currentDirection : this.directions) {
      // calculate next delta vector (0/1, 0/-1, 1/0, 1/1)
      changeDeltaVector(currentDirectionDelta, currentDirection);
      moveToNextPositions(position, currentDirectionDelta);
      if (position.x < minValue.x)
        minValue.x = position.x;

      if (position.y < minValue.y)
        minValue.y = position.y;
    }

    return minValue;
  }

  // Get max coordinates point in folding for x and y
  public Point getMaxValue() {
    Point currentDirectionDelta = new Point(0, 1);
    Point position = new Point(0, 0);

    Point maxValue = new Point(0, 0);

    for (Direction currentDirection : this.directions) {
      // calculate next delta vector (0/1, 0/-1, 1/0, 1/1)
      changeDeltaVector(currentDirectionDelta, currentDirection);
      moveToNextPositions(position, currentDirectionDelta);
      if (position.x > maxValue.x)
        maxValue.x = position.x;

      if (position.y > maxValue.y)
        maxValue.y = position.y;
    }

    return maxValue;
  }


  // P3
  // ================================================================================================================
  // Mutate a direction at a random place of the folding - change 1 direction
  public void mutate() {
    int position = new Random().nextInt(directions.size());
    this.directions.set(position, Direction.getRandomDirection());
    this.fitness = -1; // reset fitness to -1 for another analyze
  }


  // Crossover directions of two foldings of same size (we have the same sequence, nothing to change here) - crossover directions
  public void crossover(Folding toCrossoverWith, int positionToCross) {
    int size = directions.size();

    List<Direction> tmp = new ArrayList<>(toCrossoverWith.getDirections().subList(positionToCross, size)); // save crossover content of other folding

    for (int i = positionToCross; i < size; i++) {
      toCrossoverWith.directions.set(i, directions.get(i)); // set directions of other folding
      directions.set(i, tmp.get(i - positionToCross));      // set directions of this folding from tmp
    }

    this.fitness = -1; // reset fitness of both to -1 for next evaluation
    toCrossoverWith.fitness = -1;
  }
}
