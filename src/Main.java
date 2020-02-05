import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

public class Main {
	static int numOfNodes;
	static Solution[] solutions2;
	static int[][] matrix = new int[1000][1000];
	static int nodeWeight[] = new int[1000];
	static double crossoverProb, mutationProb;
	static int popSize, generationNum;
	
	public static void main(String[] args) throws IOException {
		// Get inputs from user
		Scanner myObj = new Scanner(System.in);
		
		// Get filename
		System.out.print("Enter name of the file: ");
		String filename = myObj.nextLine();
		
		// Get number of generations
		System.out.print("Enter number of generations: ");
		generationNum = Integer.parseInt(myObj.nextLine());
		
		// Get population size
		System.out.print("Enter population size: ");
		popSize = Integer.parseInt(myObj.nextLine());
		
		// Get crossover probability
		System.out.print("Enter crossover probability: ");
		crossoverProb = Double.parseDouble(myObj.nextLine());
		crossoverProb = crossoverProb * 100;
		
		// Get mutation probability
		System.out.print("Enter mutation probability: ");
		mutationProb = Double.parseDouble(myObj.nextLine());
		mutationProb = mutationProb * 1000;
		
		// Initialize object arrays
		Solution[] solutions = new Solution[popSize];
		solutions2 = new Solution[popSize];
		Solution[] solutions3 = new Solution[popSize];
		Solution[] temporary = new Solution[popSize];
		
		// Construct adjacency matrix from text file
		getMatrix(filename);
		
		// Initialize each component of the array
		setObjectArray(solutions);
		setObjectArray(temporary);
		
		PrintWriter writer = new PrintWriter("average.txt", "UTF-8");	// Writes the average fitness score of each generation to average.txt
	
		// Create random solutions
		String temp = null;
		for(int k=0;k<popSize;k++) {	// Repeat popSize times
			temp = createSolution();	// Create solution and set it to temp
			temp = repair(temp);    // Repair solution and make it feasible
			solutions[k].solution = temp;	// Set solution to solutions array
			solutions[k].fitnessScore = calculateFitness(temp);	// Calculate fitness of the array
			
		}
		writer.println(calculateAverage(solutions));
		createNextGen(solutions, solutions3);	// Create next generation and set it to solutions3
		
		for(int k=0;k<generationNum-1;k++) {	// From here generation creation is looped
			System.out.println("At gen: " + (k+2));	// Prints generation number for to console for convenience
			copyArray(solutions3, temporary);	// Copy current generation to temporary array
			createNextGen(temporary, solutions3);	// Create next generation from temporary and save it to solutions3
			if(k==generationNum-2) {
				System.out.println("Best found solution in last generation is:" + pickBest(solutions3));
				System.out.println(pickBestSol(solutions3));
			}
			writer.println(calculateAverage(solutions3));
			
		}
		writer.close();
	}	// End of main method
	
	//	Calculates average fitness score of each generation for graphs.
	public static double calculateAverage(Solution[] solutions) {
		double average = 0;
		for(int k=0;k<popSize;k++) {
			average = average + solutions[k].fitnessScore; 
		}
		average = average / popSize;
		double temp2 = 0;
		String temp = "" + average;
		temp2 = Double.parseDouble(temp.substring(temp.indexOf('.')+1,temp.indexOf('.')+3));
		average = (int) average;
		average = average + (temp2 / 100);
		
		System.out.println("Average is:" + average);
		return average;
	}
	
	
	// Copies source array to destination array
	public static void copyArray(Solution[] source, Solution[] dest) {
		for(int k=0;k<popSize;k++) {
			dest[k] = source[k];
		}
	}
	
	// Returns the best solution in the latest generation
	public static String pickBestSol(Solution[] solutions3) {
		double best = 99999;
		String bestSol = "";
		for(int k=0;k<popSize;k++) {
			if(solutions3[k].fitnessScore < best) {
				best = solutions3[k].fitnessScore;
				bestSol = solutions3[k].solution;
			}
		}
		return bestSol;
	}
	
	// Returns the last geenration's best solution's fitness score
	public static double pickBest(Solution[] solutions3) {
		double best = 99999;
		for(int k=0;k<popSize;k++) {
			if(solutions3[k].fitnessScore < best) 
				best = solutions3[k].fitnessScore;
		}
		return best;
	}
	
	// Creates a new generation from solutions array and sets it to solutions3 array
	public static void createNextGen(Solution solutions[], Solution solutions3[]) {
		Solution[] solutions2 = new Solution[popSize];	// Temporary solutions array
		setObjectArray(solutions2);	// Initialize temporary array
		setObjectArray(solutions3); // Initialize goal array
		
		// Randomly pick 2 solutions from current gen pool and select one using binary tournament selection method
		for(int k=0;k<popSize;k++) {	// Repeat popSize times
			solutions2[k] = tournamentSel(solutions);	// solutions2 is filled with tournament winners
		}
		
		// Crossover
		// Create temporary solution objects
		Solution father = new Solution();
		Solution mother = new Solution();
		Solution child1 = new Solution();
		Solution child2 = new Solution();
		
		int n = 0;
		for(int k=0; k<popSize/2;k++) {
			father = solutions2[pickParent()];	// Randomly pick parents from parent pool (tournament winners)
			mother = solutions2[pickParent()];
			child1 = createChild(father,mother);	// Create child 1
			child2 = createChild(mother,father);	// Create child 2 with reverse crossover
			
			// Decide whether to send parents or children to next generation
			Random rand = new Random();
			
			if(rand.nextInt(100) < crossoverProb) {	// For crossover probability 0.5, crossoverProb is = 50, therefore %50 chance of parents to advance to next gen
				// Send children to next gen
				solutions3[n] = child1;
				n++;
				solutions3[n] = child2;
				n++;
			}
			else {
				// Send parents to next gen
				solutions3[n] = father;
				n++;
				solutions3[n] = mother;
				n++;
			}
		}
		
		// Mutation
		for(int k=0;k<popSize;k++) {
			Random rand = new Random();
			for(int m=0;m<1000;m++) {	// For every node in a single solution, there is mutationProb chance of being flipped( '1' to '0' or '0' to '1')
				if(rand.nextInt(1000) > (999-mutationProb))
					solutions3[k].solution = mutate(solutions3[k].solution,m);
			}	
		}
		// Repair solutions and finalize generation
		String temp;
		for(int k=0;k<popSize;k++) {
			temp = solutions3[k].solution;
			temp = repair(temp);
			solutions3[k].solution = temp;
			solutions3[k].fitnessScore = calculateFitness(temp);
		}
		printScore(solutions3);
	}
	
	// Repair function. Repairs a given solution s and returns the fixed solution
	public static String repair(String s) {
		int[] selectedNodes = new int[1000];	// Each element either equals to 0 (means the node is not included in the tower) or 1 (included)
		int[] ones = new int[1000];	 // Each element is either 1 (if the node is selected) or 0 (unselected)
		int[] excluded = new int[1000];	// Either -1 or keeps the number of an excluded node
		
		for(int k=0;k<1000;k++) {	// Set selectedNodes and ones arrays
			if(s.charAt(k) == '1') {
				selectedNodes[k] = 1;
				ones[k] = 1;
				// If a node is 1, add the nodes it has an edge with to selectedNodes array as well
				for(int m=0; m<1000;m++) {	// Iterate through all nodes
					if(matrix[k][m] == 1) {	// Check if there exists an edge
						selectedNodes[m] = 1;	// Add that node to selectedNodes as well
					}
				}
			}
		}
		
		int a = 0;	// Keeps track of excluded index
		setexcluded(excluded);	// Set all elements of excluded array to -1 (to avoid confusion with number 0 node)
		
		for(int k=0;k<1000;k++) {	// Loop through all nodes
			if(selectedNodes[k] == 0) {	// If there exists a node that is not in the tower add it to excluded list
				excluded[a] = k;
				a++;
			}
		}
		shuffleArray(excluded);	// Shuffle excluded (so that repair process won't have a pattern of start, thus conserved randomization is achieved)
		
		for(int k=0;k<1000;k++) {	// Loop through excluded array
			if(excluded[a] != -1) {	// If array value is other than -1, it means excluded[a] is a node that is not in the tower
				ones[excluded[a]] = 1;	// Set that excluded node to 1
				selectedNodes[excluded[a]] = 1;	// Add it to selectedNodes array as well
				
				for(int m=0; m<1000;m++) {	// Iterate through all nodes
					if(matrix[excluded[a]][m] == 1) {	// Check if there exists an edge
						if(isInexcluded(excluded, m))	//  Check if it is already in the excluded array
							deleteFromexcluded(excluded, m);	// If it is, delete it (no need to set it to 1)
						selectedNodes[m] = 1;	// Add that node to whiteList as well
					}
				}
			}
			// In rare cases, deleteFromexcluded might cause a few nodes to be left outside tower. Check for those nodes and if found set those to 1
			for(k=0; k<1000; k++) {
				if(selectedNodes[k] == 0)
					ones[k] = 1;
			}
		}
		
		// Construct new feasible solution from ones array
		String temp = "";
		for(int k=0;k<1000;k++) {
			if(ones[k] == 1)
				temp = temp + "1";
			else
				temp = temp + "0";
		}
		return temp;	// Return new feasible solution
	}	// End of repair method
	
	
	// Prints solutions of a generation
	public static void printSolution(Solution[] s) {
		for(int k=0;k<popSize;k++)
			System.out.println(s[k].solution);
	}
	
	// Prints fitness scores of a generation
	public static void printScore(Solution[] s) {
		for(int k=0;k<popSize;k++)
			System.out.println((double) (s[k].fitnessScore));
	}
	
	// Initializes each component in parameter array
	public static void setObjectArray(Solution[] solutions) {
		for(int k=0;k<popSize;k++) {
			solutions[k] = new Solution();
		}
	}
	
	// Creates a child solution from mother and father solution
	public static Solution createChild(Solution father, Solution mother) {
		Random rand = new Random();
		String fatherGenes, motherGenes;
		int onePoint = rand.nextInt(1000);	// Randomly pick a point to divide string
		Solution child = new Solution();
		fatherGenes = father.solution.substring(0,onePoint);	// Get first half from father
		motherGenes = mother.solution.substring(onePoint,mother.solution.length());	// Get second half from mother
		child.solution = fatherGenes + motherGenes;	// Concatenate strings
		child.fitnessScore = calculateFitness(child.solution);	// Calculate solution's fitness score
		return child;	// Return child solution
	}
	
	// Pick a random number from 0 to popSize. Used for picking a random parent
	public static int pickParent() {
		Random rand = new Random();
		return rand.nextInt(popSize);
	}
	
	// Randomly selects two solutions to duel, returns winner
	public static Solution tournamentSel(Solution[] solutions) {
		Random rand = new Random();
		int random = rand.nextInt(popSize);
		int random2 = rand.nextInt(popSize);
		if(solutions[random].fitnessScore > solutions[random2].fitnessScore)
			return solutions[random2];
		else
			return solutions[random];
	}
	
	// Calculates fitness score of a solution string
	public static double calculateFitness(String s) {
		double totalFitness = 0;
		for(int k=0;k<1000;k++) {
			if(s.charAt(k) == '1') {
				totalFitness = totalFitness + nodeWeight[k];
			}
		}
		return (totalFitness / 100.0);
	}
	
	// Modifies a character of a solution string by turning it to a char array then returning it as a string
	public static String modifyChar(String solution, int index) {
		char[] temp = solution.toCharArray();
		temp[index] = 48;	// ASCII value of 1
		return String.valueOf(temp);
	}
	
	// Mutates(flips) a node of a string to opposite value at its index
	public static String mutate(String solution, int index) {
		char[] temp = solution.toCharArray();
		if(temp[index] == '1')
			temp[index] = '0';
		else
			temp[index] = '1';
		return String.valueOf(temp);
	}
	
	
	// Checks if the parameter node is in excluded array
	public static boolean isInexcluded(int[] excluded, int node) {
		for(int k=0;k<1000;k++) {
			if(excluded[k] == node)
				return true;
		}
		return false;
	}
	
	// Deletes the found node in the list
	public static void deleteFromexcluded(int[] excluded, int node) {
		for(int k=0;k<1000;k++) {
			if(excluded[k] == node)
				excluded[k] = -1;
		}
	}
	
	// Shuffles given int array
	public static void shuffleArray(int[] ar) {
		Random rnd = new Random();
	    for (int i = ar.length - 1; i > 0; i--) {
	    	int index = rnd.nextInt(i + 1);
	    	// Simple swap
	    	int a = ar[index];
	    	ar[index] = ar[i];
	    	ar[i] = a;
	    }
	}
	
	// Sets all elemets of excluded array to -1
	public static void setexcluded(int[] excluded) {
		for(int k=0;k<1000;k++) {
			excluded[k] = -1;
		}
	}
	
	// Initializes 1000x1000 adjaceny matrix from textfile "filename"
	public static void getMatrix(String filename) throws IOException {
		FileInputStream fstream = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine, temp;
		int destNode, currentNode;
		 
		// Skip first 2 lines
		strLine = br.readLine();
		numOfNodes = Integer.parseInt(strLine);
		strLine = br.readLine();
		 
		// Get weights of nodes
		for(int k=0; k<numOfNodes; k++) {
			strLine = br.readLine();
			// strsize -2 to strsize
			strLine = strLine.substring(strLine.length()-2,strLine.length());
			nodeWeight[k] = Integer.parseInt(strLine); 
		}
		 
		// Get all edges
		while ((strLine = br.readLine()) != null)   {
			temp = strLine.substring(0, strLine.indexOf(" "));
			currentNode = Integer.parseInt(temp);
			strLine = strLine.substring(strLine.indexOf(" ")+1,strLine.length());
			destNode = Integer.parseInt(strLine);
			matrix[currentNode][destNode] = 1;
		}
		fstream.close();
	}
	
	// Creates a random solution that is (probably) not feasible and returns it
	public static String createSolution() {
		Random rand = new Random();
		int random;
		String solution = "";
		// Construct a string of size 1000 of zeroes and ones
		for(int k=0;k<1000;k++) {
			random = rand.nextInt(2);
			solution = solution + "" + random;
		}
		return solution;
	}
}
