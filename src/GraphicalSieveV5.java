import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.TimeUnit;

public class GraphicalSieveV5{
	
	//was unsure where to declare and assign this array of reds for eliminated multiples
	//decided on main, feedback would be appreciated!
	public static Color[] REDS = new Color[7];	

	public static void main(String[] args)
	{

		//takes in 1 or 3 command line arguments, 1st argument is the limit for the sieve
		//2nd and 3rd are desired x and y resolution for drawing window (helpful for large limits)
		int sieveLimit = Integer.parseInt(args[0]);

		if(args.length == 3)
			StdDraw.setCanvasSize(Integer.parseInt(args[1]), Integer.parseInt(args[2]));

		//Have to set frame field as public in StdDraw if you want this to work!
		StdDraw.frame.setTitle("Sieve of Eratosthenes Visualisation (" + sieveLimit + ")");

		int[] numberArray = new int[sieveLimit-1];

		//this finds the square number that fits our sieve limit
		int sqrRtToFit = (int)Math.sqrt(sieveLimit);
		while(sieveLimit > sqrRtToFit*sqrRtToFit)
			sqrRtToFit++;

		int boxesX=sqrRtToFit;
		int boxesY=sqrRtToFit;

		//if a whole row of the array can be eliminated and still fit the sieve limit
		//this code does so
		while(boxesX*(boxesY-1) >= sieveLimit)
			boxesY--;


		Box[][] boxArray = new Box[boxesX][boxesY];

		//was unsure where to declare and assign this array of reds for eliminated multiples
		//decided on main, feedback would be appreciated!
		for(int i = 0; i < 7; i++)
		{
			REDS[i] = new Color((115 + 20*i), 40, 40);
		}

		//a dark grey as a default for boxes
		Color grey = new Color(20,20,20);

		//the boxes start at box "1" in the top left corner
		int startingNumber = 1;

		//these control the total amount of whitespace around and between the squares
		//they *must* add to 1
		double margin = 0.1 / boxesX;
		double boxSize = 0.9 / boxesX;

		for(int Y = 0; Y < boxesY; Y++)
		{
			for(int X = 0; X < boxesX; X++)
			{
				boxArray[X][Y] = new Box(boxSize*X + margin*X+boxSize/2, //x-coord of center of box
						1 - boxSize*Y - margin*Y -boxSize/2, //y-coord of center of box
						boxSize/2, 							 //radius of box
						grey, 								 //start color of boxes
						startingNumber);					 //number contained within

				//"hides" all boxes past the sievelimit rather than leaving null Box references in array
				if(startingNumber > sieveLimit)
					boxArray[X][Y].color=Color.WHITE;

				startingNumber++;

			}
		}

		//hides box "1"
		boxArray[0][0].color = Color.WHITE;

		//draws all the boxes
		drawBoxes(boxArray);


		//this creates the number array that actually finds the primes
		//operations could be performed using maths and indexes of the boxes
		//but felt this was unintuitive and error prone
		startingNumber = 2;
		for(int index = 0; index < (sieveLimit - 1); index++)
		{
			numberArray[index] = startingNumber++;
		}


		findPrimes(numberArray, boxArray);
		printNumbers(numberArray);

	}



	public static void drawBoxes(Box[][] boxArray)
	{
		//N.B. font is in proportion to box size so readable with large limits
		Font textFont = new Font("SansSerif", Font.PLAIN, 6 + 80/boxArray.length);
		int boxesX = boxArray.length;
		int boxesY = boxArray[0].length;
		for(int rows = 0; rows < boxesX; rows++)
		{
			for(int columns = 0; columns < boxesY; columns++)
			{
				//takes co-ords and color of each box, draws them
				StdDraw.setPenColor(boxArray[rows][columns].color);
				StdDraw.filledSquare(boxArray[rows][columns].x, boxArray[rows][columns].y, boxArray[rows][columns].size);
				//and adds text for number
				StdDraw.setPenColor(Color.WHITE);
				StdDraw.setFont(textFont);
				StdDraw.text(boxArray[rows][columns].x, boxArray[rows][columns].y,boxArray[rows][columns].sNumber);
			}
		}
	}

	//method mostly useful for debugging, prints correct primes regardless of visual display
	public static void printNumbers(int[] numberArray)
	{

		for(int index = 0; index < numberArray.length; index++)
		{
			if(numberArray[index] != -1)

			{
				System.out.print((index == 0? "":", ") + numberArray[index]);
			}
		}
	}
	
	
	public static void findPrimes(int[] numberArray, Box[][] boxesArray)
	{
		//computes square root of limit, erring on the side of caution with double fuzziness and conversion to int
		double squareRootOfLimit = Math.sqrt(numberArray.length + 1);
		squareRootOfLimit += 0.5;
		int squareRootOfLimitInt = (int) squareRootOfLimit;

		for(int index = 0; index < squareRootOfLimitInt; index++)
		{
			if(numberArray[index] != -1)
			{	
				//waits a quarter second when finding a new prime so that eliminating its multiples is more obvious
				try {
					TimeUnit.MILLISECONDS.sleep(250);
				} catch (InterruptedException e) {
					//helpful error message, try-catch demanded by method
					System.out.print("Something terrible has happened! 'sleep' error description follows :");
					e.printStackTrace();
				}
				markOffMultiples(numberArray[index], index, numberArray, boxesArray);
			}
		}
	}

	public static void markOffMultiples(int prime, int index, int[] numberArray, Box[][] boxArray)
	{
		//marks off multiples of primes by adding the value of the prime to its original index 
		//and flagging each multiple as -1 until index would be outside of array

		while (index < numberArray.length - prime)
		{
			index += prime;
			numberArray[index] = -1;
			int boxIndex = index+1;
			//calculates co-ords of correct box in array to change color, does so and draws it
			int boxY  = boxIndex / boxArray.length;
			int boxX = boxIndex % boxArray.length;
			boxArray[boxX][boxY].color = REDS[prime % 7];
			drawBox(boxArray[boxX][boxY]);
		}
	}
	
	//draws an individual box for when a multiple of a prime is being eliminated
	public static void drawBox(Box box)
	{
		
		//this method uses the size of the box (inversely proportional to the square of the number of boxes)
		//to calculate how much time it should wait before drawing for a satisfying visual effect
		//eg, if limit is 1000, boxes are drawn extremely quickly, 
		//    if limit is 10 boxes are drawn slowly for clarity
		try {
			TimeUnit.MILLISECONDS.sleep((int)(30000*box.size*box.size));
		} catch (InterruptedException e) {
			//helpful error message, try-catch demanded by method
			System.out.print("Something terrible has happened! 'sleep' error description follows :");
			e.printStackTrace();
		}
		StdDraw.setPenColor(box.color);
		StdDraw.filledSquare(box.x, box.y, box.size);
	}



}
