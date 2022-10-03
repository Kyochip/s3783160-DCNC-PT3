package Main;

import java.io.PrintStream;
import java.util.Scanner;

public class Menu {

	private static final int GENERATE_HAMMING_CODE = 1;
	private static final int QUIT = 9;

	private final Scanner scanner = new Scanner(System.in);

	public void run() {
		menuOptions();
	}

	private void menuOptions() {
		boolean runMenu = true;
		int integerInput = 0;
		String input = null;
		while (runMenu) {
			printMenu();
			System.out.print("Select an option: ");
			input = scanner.nextLine();
			try {
				integerInput = Integer.parseInt(input);

				if (integerInput == GENERATE_HAMMING_CODE) {
					generateHammingCode();
				} else if (integerInput == QUIT) {
					System.out.println("Program ending.");
					System.exit(0);
				} else {
					printErrorMessage();
				}
			} catch (Exception e) {
				printErrorMessage();
			}

		}

	}

	private void printMenu() {
		System.out.println();
		System.out.println("--- Options ---");

		String format = "%-5s %s\n";

		System.out.printf(format, GENERATE_HAMMING_CODE, "Generate Hamming Code");
		System.out.printf(format, QUIT, "Quit\n");
	}

	private void generateHammingCode() {
		boolean loop = true;
		int noDataBits = 0;
		int dataWord[] = new int[noDataBits];
		int noParBit = 0;
		int senderCodeWord[] = new int[noDataBits + noParBit];
		int recCodeWord[] = new int[noDataBits + noParBit];
		System.out.println("\n--- HAMMING CODE ---\n");
		System.out.println("For SENDER:\n");

		while (loop) {
			noDataBits = Integer.parseInt(checkString("Enter # of bits of data word: ", scanner));

			if (noDataBits < 3)
				printErrorMessage("# data bits can not be less than 3");

			else {
				dataWord = new int[noDataBits];

				noParBit = calcNoParBits(noDataBits);
				System.out.println("Number of Parity bits are: " + noParBit);

				System.out.println("\nEnter Data word:");
				for (int i = 0; i < noDataBits; i++) {
					loop = true;
					while (loop) {
						int input = Integer.parseInt(checkString("d" + (i + 1), scanner));
						if (checkDigitInput(input) != 2) {
							dataWord[i] = checkDigitInput(input);
							loop = false;
						}
					}
				}
				
				senderCodeWord = new int[noDataBits + noParBit];
				calcSenderCodeWord(senderCodeWord, dataWord, noParBit, noDataBits);
				System.out.println("Sender Code Word:");
				codeWord(senderCodeWord, noDataBits, noParBit);
				for (int i = 0; i < (noDataBits + noParBit); i++)
					System.out.print(senderCodeWord[i] + " ");

				System.out.println();
				recCodeWord = new int[noDataBits + noParBit];
				System.out.println("\nFor RECEIVER:\n");
				System.out.println("Enter Receieved Code Word: ");
				for (int i = 0; i < (noDataBits + noParBit); i++) {
					loop = true;
					while (loop) {
						int input = Integer.parseInt(checkString("bit #" + (i + 1), scanner));
						if (checkDigitInput(input) != 2) {
							recCodeWord[i] = checkDigitInput(input);
							loop = false;
						}
					}
				}
				int redBits[] = new int[noParBit];
				boolean err = detectError(recCodeWord, redBits, noDataBits, noParBit);
				if (err) {
					System.out.println("\nNo error in transmission.");
					System.out.println("\nExtracted Data word is:");
					for (int i = 0; i < noDataBits; i++)
						System.out.print(dataWord[i] + " ");
					System.out.println();
				} else {
					System.out.println("\nError found in data transmission.\n----------------------------------");
					System.out.println();
					correctError(recCodeWord, redBits, dataWord, noDataBits, noParBit);
					System.out.println("Code Word after Error correction is:");
					for (int i = 0; i < (noDataBits + noParBit); i++)
						System.out.print(recCodeWord[i] + " ");
					System.out.println();
					System.out.println("\nExtracted Data word is:");
					for (int i = 0; i < noDataBits; i++)
						System.out.print(dataWord[i] + " ");
					System.out.println();
				}
			}
		}
	}

	public static int calcNoParBits(int noDataBits) {
		int r = 1;
		for (int i = 1; i <= noDataBits; i++) {
			if (Math.pow(2, i) >= (noDataBits + i + 1)) {
				r = i;
				break;
			} else
				continue;
		}
		return r;
	}

	public static void calcSenderCodeWord(int senderCodeWord[], int dataWord[], int noParBits, int noDataBits) {
		int i, j, k = noDataBits - 1, ind = 1;
		boolean flag = false;
		for (i = (noDataBits + noParBits - 1); i >= 0; i--) {
			flag = false;
			for (j = 0; j < ind; j++) {
				if ((int) Math.pow(2, j) == ind) {
					flag = true;
					break;
				}
			}
			if (flag) {
				senderCodeWord[i] = 0;
			} else {
				senderCodeWord[i] = dataWord[k];
				k--;
			}
			ind++;
		}
	}

	public static void codeWord(int senderCodeWord[], int noDataBits, int noParBits) {
		System.out.println("\nRedundant Bits:");
		int i, ind = 1, j, k, xor = 0, count;
		boolean flag = false;
		for (i = (noDataBits + noParBits - 1); i >= 0; i--) {
			flag = false;
			xor = 0;
			for (j = 0; j < ind; j++) {
				if ((int) Math.pow(2, j) == ind) {
					flag = true;
					break;
				}
			}
			if (flag) {
				k = noDataBits + noParBits - ind;
				count = ind;
				while (k >= 0) {
					xor = xor ^ (senderCodeWord[k]);
					k--;
					count--;
					if (count == 0) {
						k = k - ind;
						count = ind;
					}
				}
				System.out.println("R" + (i - (noDataBits + noParBits)) + " = " + xor);
				senderCodeWord[i] = xor;
			}
			ind++;

		}
		System.out.println();
	}

	public static boolean detectError(int recCodeWord[], int redBits[], int noDataBits, int noParBits) {
		codeWord(recCodeWord, noDataBits, noParBits);
		int i, j, ind = 1, rind = noParBits - 1;
		boolean flag = false;
		for (i = (noDataBits + noParBits - 1); i >= 0; i--) {
			flag = false;
			for (j = 0; j < ind; j++) {
				if ((int) Math.pow(2, j) == ind) {
					flag = true;
					break;
				}
			}
			if (flag) {
				redBits[rind] = recCodeWord[i];
				rind--;
			}
			ind++;
		}
		for (i = 0; i < noParBits; i++) {
			if (redBits[i] == 1) {
				return false;
			}
		}
		return true;
	}

	public static void correctError(int recCodeWord[], int redBits[], int dataWord[], int noDataBits, int noParBits) {
		String binary = "";
		for (int i = 0; i < noParBits; i++) {
			binary = binary + redBits[i];
		}
		int decimal = Integer.parseInt(binary, 2);
		System.out.println("Error detected at bit position " + decimal);
		int index = noDataBits + noParBits - decimal;
		if (recCodeWord[index] == 0) {
			recCodeWord[index] = 1;
		} else {
			recCodeWord[index] = 0;
		}
		int i, j, ind = 1, dind = noDataBits - 1;
		boolean flag = false;
		for (i = (noDataBits + noParBits - 1); i >= 0; i--) {
			flag = false;
			for (j = 0; j < ind; j++) {
				if ((int) Math.pow(2, j) == ind) {
					flag = true;
					break;
				}
			}
			if (flag) {
				continue;
			} else {
				dataWord[dind] = recCodeWord[i];
				dind--;
			}
			ind++;
		}
	}

	public String checkString(String promt, Scanner sc) {

		System.out.printf("%-20s", promt);
		String input = sc.nextLine();
		if (input.isEmpty() || input.isBlank() || input.equalsIgnoreCase(null)) {
			menuOptions();
		}
		return input;
	}

	public int checkDigitInput(int input) {
		if (input != 0 && input != 1) {
			printErrorMessage("Must enter either 0 or 1");
			return 2;
		}

		return input;
	}

	private static void printErrorMessage() {
		printErrorMessage("Invalid, please try again.");
	}

	private static void printErrorMessage(Exception e) {
		printErrorMessage(String.format("Failed due to: %s", e.getMessage()));
	}

	private static void printErrorMessage(String message) {
		flushAndSleep(System.out);
		System.err.println(message);
		flushAndSleep(System.err);
	}

	private static void flushAndSleep(PrintStream stream) {
		// 50ms sleep.
		final int DEFAULT_SLEEP = 50;

		// The flush and sleep below is only present to help the Eclipse console
		// on Windows to output System.out and System.err streams in the
		// expected order.
		stream.flush();
		try {
			Thread.sleep(DEFAULT_SLEEP);
		} catch (InterruptedException e) {
		}
	}
}
