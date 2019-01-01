/*Program to scan state bank balance sheets for 1892 Comptroller of the Currency Report and export data to excel. 
 * This template can be adjusted to scan different years. 
 * Author: Thomas Nemeh. Summer 2017 */

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class DataExtractorV2 {
	/* Unfortunately Java does not have pass by reference, making global variables the best option*/
	
	// Scanner to read input from Comptroller of Currency Report
	private static Scanner input = null;
	
	// Track when we must take actions associated with transitioning to a new page in the report ??????????????????
	private static boolean newPage = false;
	
	// Current bank that we are scanning
	private static Bank currentBank = null;
	
	// Track when the bank we are scanning is the first one on the page, since these contain an additional line between the header and the start of the balance sheet ?????
	private static boolean firstBankOnPage true;
	
	// Indicate when there is an error on these lines, a common occurance. Note: line one begins at loans and discounts.
	private static boolean line3Or4Error = false;
	
	// Indicate that there is an error with a number
	private static boolean error = false;
	
	// Workbook for excel file
	private static HSSFWorkbook workbook = null;
	
	// Sheet for excel file
    private static HSSFSheet sheet = null;
	
	// Count the row we are on in our excel file
    private static int excelRowCounter = 1;
    
	// Page number 
    private static String page = "";
	
	//indicate when we must skip the next two fields to accomodate errors in spacing
	private static twoLineCount = 0;
	
	private static String data[] = null;
    
	/* Main method- scans each bank and produces an excel spread sheet with the field of each bank */
	public static void main(String[] args) {
		input = null;   
	    try {
	        input = new Scanner(new File("FullDocFinal.txt"));                                                   
	    } 
	    catch (FileNotFoundException e) {
	        System.out.println("Problem opening file: " + e.getMessage());
	        System.exit(1);
	    }
	   
		// Setup the excel file
	    createExcelFile();
	    
		// Scan each bank, adding data to our excel file along the way
	    while (input.hasNextLine()) {
		   String page = input.nextLine();
		   if (page.contains("Page")) {
			   System.out.println(page);
			   setPageNumber(page);
			   scanNewPage();
		   }
	   }
	   
	   // Output excel sheet in folder where project is stored
	   finalizeExcelSheet();
	}
	
	// With our scanner at the top of the page, skip to the first bank and begin scanning each balance sheet
	public static void scanNewPage() {
		int numLines = 0;
		String state = null;
		
		// Skip header line and and get the U.S. state listed at top of the page
		while (numLines < 2) {
			state = input.nextLine();
			
			// trailing and leading whitespace
			state = state.trim();
			
			// If the line has more than 10 letters, we know it is the header line- too long to be the state
			if (state.length() > 10) {
				numLines++;
			}
		}
		
		//remove spaces in between letters		
		state = state.replaceAll("\\s+","");
		
		//remove period at the end of the state
		if (! (Character.isLetterOrDigit(state.charAt(state.length() - 1)))) {
			state = state.substring(0, state.length() - 1);
		}
		
		newPage = false;
		firstBankOnPage = true;
		
		//System.out.println(state);
		
		//While we are on the page, scan each bank and transfer it to our excel file
		while (newPage == false) {
			scanBank(state, firstBankOnPage);
			if (currentBank != null) {
				bankToExcel();
			}
			//System.out.println("*****************************************");
		}
	}
	
	// Get each field in the bank balance sheet and store in Bank Instance
	public static void scanBank(String state) {
		
		//Get header information
		currentBank = new Bank();
		String line = getNextLine(1);
		//System.out.println(line);
		
		// We have reached the watermark at the end of the page. We must check if we have reached the end of the page several times in order to account for irregularities in spacing
		if (line.contains("Digitized")) {
			currentBank = null;
			newPage = true;
			return;
		}
		
		// Set state, name of bank, and city
		currentBank.setState(state);
		setNameAndCity(line);
		
		if (line.contains("Digitized")) {
			currentBank = null;
			newPage = true;
			return;
		}
		
		//get bank number
		line  = getNextLine(1);
		setBankNumber(line);
		
		if (line.contains("Digitized")) {
			currentBank = null;
			newPage = true;
			return;
		}
		
		//Get first half of bank balance sheet
		getFirstHalf();
	
		// Test whether this balance sheet has a certified checks field, which influences how the subsequent balance sheet is structured
		boolean includeCertifiedChecks = false;
		line = getNextLine(1);
		line2 = getNextLine(1);
		line3 = getNextLine(1);
		line3 = line3.trim();
		//System.out.println(line3);
		if (line3.length() > 80) {
			includeCertifiedChecks = true;
		}
		
		// Scan balance sheet without certified checks field 
		if (includeCertifiedChecks == false) {
			getSecondHalfNoCertified();
		}
		// Scan balance sheet with certified checks field
		else {
			getSecondHalfWithCertified();
		}
	}
	
	//Get first 11 fields in the balance sheet, up to and including 'Other real estate and mortge's owned'
	public static void getFirstHalf() {
		if (firstBankOnPage == true) {
			line = getNextLine(2);
			firstBankOnPage = false;
		}
		else {
			line = getNextLine(1);
		}
		//Spacing errors often occur on first line, necessitating separate method
		setLine1(line);
		
		// Get overdrafts
		line = getNextLine(1);
		String number = singleEntryLine(line, "Overdrafts");
		singleEntrySkip(number);
		
		if (twoLineCount >= 2) {
			currentBank.setOverdrafts(number);
		}
		twoLineCount++;
		
		// Get bonds to secure circulation and bonds to secure deposits, spacing errors often occur here, necessitating separate method
		line = getNextLine(1);
		String line2 = getNextLine(1);
		String line3 = getNextLine(1);
		setLines3And4(line, line2, line3);
		
		//Get bonds on hand
		line = null;
		if (line3Or4Error == false) {
			currentBank.setBondsOnHand(singleEntryLine(line3, "Bonds on hand"));
		}
		else {
			line = getNextLine(1);
			currentBank.setBondsOnHand(singleEntryLine(line, "Bonds on hand"));
		}
		line3Or4Error = false;
		
		// Get stocks, securities, etc. and national bank notes outstanding
		line = getNextLine(1);
		data = doubleEntryLine(line, "Stocks, securities, etc", "National bank notes outstanding");
		doubleEntrySkip(data);
		if (twoLineCount >= 2) {
			currentBank.setStocksSecurities(data[0]);
			currentBank.setNationalBankNotes(data[1]);
		}
		twoLineCount++;		
		
		// Get due from approved reserve agents and state-bank notes outstanding
		line = getNextLine(1);
		data = doubleEntryLine(line, "Due from approved reserve  agents", "State-bank notes outstanding");
		doubleEntrySkip(data);
		if (twoLineCount >= 2) {
			currentBank.setReserveAgents(data[0]);
			currentBank.setStateBankNotes(data[1]);
		}
		twoLineCount++;
		
		// Get due from other national banks
		line = getNextLine(1);
		number = singleEntryLine(line, "Due  from other  national  banks");
		singleEntrySkip(number);
		if (twoLineCount >= 2) {
			currentBank.setNationalBanks(number);
		}
		twoLineCount++;
		
		// Get due from state banks/bankers and dividends unpaid
		line = getNextLine(1);
		data = doubleEntryLine(line, "Due from State banks and bankers", "Dividends unpaid");
		doubleEntrySkip(data);
		if (twoLineCount >= 2) {
			currentBank.setStateBanks(data[0]);
			currentBank.setDividensUnpaid(data[1]);
		}
		twoLineCount++;
		
		// Get physical assets
		line = getNextLine(1);
		number = singleEntryLine(line, "Physical assets");
		singleEntrySkip(number);
		if (twoLineCount >= 2) {
			currentBank.setPhysicalAssets(number);
		}
		twoLineCount++;
		
		// Get other real estate/mortgages and individual deposits
		line = getNextLine(1);
		data = doubleEntryLine(line, "Other real estate and mortgages owned", "Individual deposits");
		doubleEntrySkip(data);
		if (twoLineCount >= 2) {
			currentBank.setRealEstate(data[0]);
			currentBank.setIndividualDeposits(data[1]);
		}
		twoLineCount++;
	}
	
	//get latter half of balance sheet without the certified checks field, starting at 'Current expenses and taxes paid'
	public static void getSecondHalfNoCertified() {
		data = doubleEntryLine(line, "Current expenses and taxes paid", "United States deposits");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setExpensesAndTaxes(data[0]);
				currentBank.setUnitedStatesDeposits(data[1]);
			}
			twoLineCount++;
			
			// Get premiums on U.S. bonds and dposits of disbursing officers
			data = doubleEntryLine(line2, "Premiums on U. S. bonds", "Deposits of U.S. disbursing officers");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setBondPremiums(data[0]);
				currentBank.setDisbursingDeposits(data[1]);
			}
			twoLineCount++;
			
			// Get checks and other cash items
			number = singleEntryLine(line3, "Checks and other cash items");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setCashItems(number);
			}
			twoLineCount++;
			
			//Get exchanges for clearing house and due to other national banks
			line = getNextLine(1);
			data = doubleEntryLine(line, "Exchanges for clearing house", "Due  to  other national banks");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setClearingHouse(data[0]);
				currentBank.setDueToNationalBanks(data[1]);
			}
			twoLineCount++;
			
			// Get bills of other national banks and due to state banks and bankers
			line = getNextLine(1);
			data = doubleEntryLine(line, "Bills  of other national  banks", "Due to  State banks and bankers");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setOtherNationalBanks(data[0]);
				currentBank.setDueToStateBanks(data[1]);
			}
			twoLineCount++;
			
			// Get fractional currency
			line = getNextLine(1);
			number = singleEntryLine(line, "Fractional currency, nickels, cents");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setFractionalCurrency(number);
			}
			twoLineCount++;
			
			// Get specie and bills rediscounted
			line = getNextLine(1);
			data = doubleEntryLine(line, "Specie", "Notes and bills rediscounted");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setSpecie(data[0]);
				currentBank.setRediscounted(data[1]);
			}
			twoLineCount++;
			
			// Get legal tender notes and bills payable
			line = getNextLine(1);
			data = doubleEntryLine(line, "Legal tender notes", "Bills payable");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setLegalTenderNotes(data[0]);
				currentBank.setPayable(data[1]);
			}
			twoLineCount++;
			
			// Get certificates of deposit
			line = getNextLine(1);
			number = singleEntryLine(line, "U.S. certificates of deposit");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setDepositCertificates(number);
			}
			twoLineCount++;
			
			// Get redemption fund
			line = getNextLine(1);
			number = singleEntrySkip(line, "Redemption fund with Treas. U.S.");
			singleEntryLine(number);
			if (twoLineCount >= 2) {
				currentBank.setRedemptionFunds(number);
			}
			twoLineCount++;
			
			// Get treasurer dues
			line = getNextLine(1);
			number = singleEntryLine(line, "Due from Treasurer U.S.");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setTreasurerDues(number);
			}
			// Get total of balance sheet
			line = getNextLine(1);
			setTotal(line);
	}
	
	//get latter half of balance sheet with the certified checks field, starting at 'Current expenses and taxes paid'
	public static void getSecondHalfWithCertified() {
		data = doubleEntryLine(line, "Current expenses and taxes paid", "Certified checks");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setExpensesAndTaxes(data[0]);
				currentBank.setCertifiedChecks(data[1]);
			}
			twoLineCount++;
			
			//get premiums on bonds and U.S. deposits
			data = doubleEntryLine(line2, "Premiums on U. S. bonds", "United States deposits");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setBondPremiums(data[0]);
				currentBank.setUnitedStatesDeposits(data[1]);
			}
			twoLineCount++;
			
			//get cash items and deposits of disbursing officers
			data = doubleEntryLine(line3, "Checks and other cash items", "Deposits of U.S. disbursing officers");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setCashItems(data[0]);
				currentBank.setDisbursingDeposits(data[1]);
			}
			twoLineCount++;
			
			//get exchanges for clearing house
			line = getNextLine(1);
			number = singleEntryLine(line, "Exchanges for clearing house");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setClearingHouse(number);
			}
			twoLineCount++;
			
			//get bills of other national banks and due to other national banks
			line = getNextLine(1);
			data = doubleEntryLine(line, "Bills  of other national  banks", "Due  to  other national banks");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setOtherNationalBanks(data[0]);
				currentBank.setDueToNationalBanks(data[1]);
			}
			twoLineCount++;
			
			//get fractional currency and due to state banks/bankers
			line = getNextLine(1);
			data = doubleEntryLine(line, "Fractional currency, nickels, cents", "Due to  State banks and bankers");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) { 
				currentBank.setFractionalCurrency(data[0]);
				currentBank.setDueToStateBanks(data[1]);
			}
			twoLineCount++;
			
			//get specie
			line = getNextLine(1);
			number = singleEntryLine(line, "Specie");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setSpecie(number);
			}
			twoLineCount++;
			
			//get legal tender notes and notes/bills rediscounted
			line = getNextLine(1);
			data = doubleEntryLine(line, "Legal tender notes", "Notes and bills rediscounted");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setLegalTenderNotes(data[0]);
				currentBank.setRediscounted(data[1]);
			}
			twoLineCount++;
			
			//get certificates of deposits and bills payable
			line = getNextLine(1);
			data = doubleEntryLine(line, "U.S. certificates of deposits", "Bills payable");
			doubleEntrySkip(data);
			if (twoLineCount >= 2) {
				currentBank.setDepositCertificates(data[0]);
				currentBank.setPayable(data[1]);
			}
			twoLineCount++;
			
			//Get redemption fund
			line = getNextLine(1);
			number = singleEntryLine(line, "Redemption fund with Treas. U.S.");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setRedemptionFunds(number);
			}
			twoLineCount++;
			
			//get the treasurer dues
			line = getNextLine(1);
			number = singleEntryLine(line, "Due from Treasurer U.S.");
			singleEntrySkip(number);
			if (twoLineCount >= 2) {
				currentBank.setTreasurerDues(number);
			}
			
			//get the total balance
			line = getNextLine(1);
			setTotal(line);
	}
	
	// Skip line if there is an error for field with only one entry
	public static void singleEntrySkip(String number) {
		//Error 2 indicates spacing error. See, for example, page 228 balance sheet 1, Overdrafts field
		if (number != null && number.equals("Error2")) {
			//Skip the next two fields if there is a spacing error in order to avoid inaccurate information being put in a field
			twoLineCount = 0;
			input.nextLine();
			input.nextLine();
		}
		//If number is not in the valid format, leave this field blank
		if (number != null && number.equals("Error")) {
			input.nextLine();
		}
	}
	
	// Skip line if there is an error for field with 2 entries
	public static void doubleEntrySkip(String[] data) {
		if (data[1] != null && data[1].equals("Error2")) {
			twoLineCount = 0;
			input.nextLine();
			input.nextLine();
		}
		if (data[0] != null && data[0].equals("Error")) {
			input.nextLine();
		}
	}
	
	// Find next field in the balance sheet
	public static String getNextLine(int x) {
		String line = null;
		int numLines = 0;
		while (numLines < x) {
			line = input.nextLine();
			if (!(line.isEmpty())) {
				numLines++;
			}
		}
		return line;
	}
	
	/* Parse line for bank name and city
     * @param String containing bank name and city above the balance sheet*/	
	public static void setNameAndCity(String nameCity) {
		// If there is not a comma separating bank name and city, indicate that there is an error
		String bankName = "NameError";
		String bankCity = "CityError";
		
		// Get bank name before the comma and bank city after the comma
		if ((nameCity.contains(","))) {
			bankName = nameCity.substring(0, nameCity.indexOf(","));
			bankName = bankName.replaceAll("\\s+","");
			int length = bankName.length();
			for (int i = 0; i < bankName.length(); i++) {
				if (Character.isUpperCase(bankName.charAt(i))) {
					bankName = bankName.substring(0, i) + " " + bankName.substring(i);
					i++;
					length++;
				}
			}
			
			// Set bank name
			currentBank.setName(bankName);
			//System.out.println(bankName);
			bankName = null;
			
			bankCity = nameCity.substring(nameCity.indexOf(",") + 1);
			bankCity = bankCity.replaceAll("\\s+","");
			length = bankCity.length();
			for (int i = 0; i < bankCity.length(); i++) {
				if (Character.isUpperCase(bankCity.charAt(i))) {
					bankCity = bankCity.substring(0, i) + " " + bankCity.substring(i);
					i++;
					length++;
				}
			}
			
			// Remove period at end of line
			if (! (Character.isLetterOrDigit(bankCity.charAt(bankCity.length() - 1)))) {
				bankCity = bankCity.substring(0, bankCity.length() - 1);
			}
			
			// Set bank city
			currentBank.setCity(bankCity);
			//System.out.println(bankCity);
			bankCity = null;
		}
	}
	
	/* Get bank number from line containing bank president, number, and cashier
	 * @param String containing president, bank number, and cashier above bank balance sheet*/
	public static void setBankNumber(String number) {
		number =  number.replaceAll("\\s+","");
		//System.out.println(number);
		int firstDigit = -1;
		int lastDigit = -1;
		for (int i = 0; i < number.length(); i++) {
			if (Character.isDigit(number.charAt(i))) {
				if (Character.isDigit(number.charAt(i + 1))) {
					firstDigit = i;
					break;
				}
			}
		}
		String bankNumber = "";
		if (firstDigit == -1) {
			bankNumber = "NumberError1";
		}
		else {
			for (int i = firstDigit; i < firstDigit + 4; i++) {
				bankNumber = bankNumber + number.charAt(i);
				lastDigit = i;
			}
			if (!(Character.isDigit(bankNumber.charAt(3)))) {
				bankNumber = bankNumber.substring(0, bankNumber.length() - 1);
			}
			if (Character.isDigit(number.charAt(lastDigit + 1))) {
				bankNumber = "NumberError2";
			}
			
			int numDigits = 0;
			for (int i = 0; i < bankNumber.length(); i++) {
				if (!(Character.isDigit(bankNumber.charAt(i)))) {
					bankNumber = "NumberError3";
				}
			}
		}
		System.out.println(bankNumber);
		currentBank.setNumber(bankNumber);
	}
	
	/* Get loans and discounts and capital stock paid in from first line of balance sheet
	 * @param String containing first line of balance sheet */
	public static void setLine1(String line) {
		//Remove leading and trailing whitespace
		line = line.trim();
		//System.out.println(line);
		
		int firstDigitIndex = -1;
		int lastDigitIndex = -1;
		boolean error;
		
		// Get start and end index of loans and discounts $ amount between positions 48 and 68 of the line, if it exists
		String loansDiscounts = getNumLeftColumn(line, 48, 68); 
		
		if (!loansDiscounts.equals("")) {	
			loansDiscounts = loansDiscounts.replaceAll("\\s+","");
			System.out.println("Loans Discounts: " + loansDiscounts);
			loansDiscounts = format(loansDiscounts, error);
			currentBank.setLoansDiscounts(loansDiscounts);
		}
		
		// Get start and end index capital stocks $ amount at the end of the line, if it exists
		String capitalStock = "";
		firstDigitIndex = -1;
		lastDigitIndex = -1;
		int numLetters = 0;
		for (int i = line.length() - 20; i < line.length() - 8; i++) {
			if (Character.isLetter(line.charAt(i))) {
				numLetters++;
				if (numLetters == 5) {
					capitalStock = null;
					System.out.println("Capital Stock Blank");
				}
			}
			if (Character.isDigit(line.charAt(i))) {
				firstDigitIndex = i;
				break;
			}
		}
		for (int i = firstDigitIndex + 1; i <= firstDigitIndex + 20; i++) {
			if (i >= line.length()) {
				break;
			}
			if (Character.isDigit(line.charAt(i))) {
				lastDigitIndex = i;
			}
		}
		if (lastDigitIndex == -1 || firstDigitIndex == -1) {
			capitalStock = null;
			System.out.println("Capital Stock Blank");
		}
		else {
			// Get capital stock $ amount, recording if there is an error due to spacing and puntuation
			for (int i = firstDigitIndex; i <= lastDigitIndex; i++) {
				capitalStock = capitalStock + line.charAt(i);
			}
			
			error = false;
			if (lastDigitIndex + 1 < line.length() && Character.compare(line.charAt(lastDigitIndex + 1), '.') == 0) {
				capitalStock = capitalStock + ".";
			}
			else if (lastDigitIndex + 1 < line.length() && Character.compare(line.charAt(lastDigitIndex + 1), ',') == 0) {
				error = true;
			}
			if (spaceCounter(capitalStock) >= 4) {
				error = true;
			}
			if (firstDigitIndex - 4 > 0) {
				for (int i = firstDigitIndex; i > firstDigitIndex - 4; i--) {
					if(Character.isLetter(line.charAt(i))) {
						error = true;
					}
				}
			}
			
			capitalStock = capitalStock.replaceAll("\\s+","");
			System.out.println("Capital stock: " + capitalStock);
			capitalStock = format(capitalStock, error);
			currentBank.setCapitalStock(capitalStock);
		}
	}
	
	
	/* Get $ amount of lines 3 and 4, accounting for common spacing error between lines 3 and 4, or 4 and 5
	 * @param Strings representing lines 3, 4, and 5 in balance sheet */
	public static void setLines3And4(String line1, String line2, String line3) {
		boolean doubleSurplusFund = false;
		boolean doubleUndividedProfits = false;
		line1 = line1.trim();
		line2 = line2.trim();
		line3 = line3.trim();
		
		int firstDigit = -1;
		int lastDigit = -1;
		String circulationBonds = "";
		String undividedProfits = "";
		String surplusFund = "";
		String depositBonds = "";
		String num = "";
		boolean circulationBondsSet = false;
		
		// Test whether surplus fund is in propert place on line 3 and undivided profits on line 4, otherwise skip the field. 
		// Whether these fields are in their proper place are not determine how these three lines are structured
		if (line1.length() < 80) {
			doubleSurplusFund = true;
		}
		if (line2.length() < 80) {
			doubleUndividedProfits = true;
		}
		
		// Case 1: see page 7 balance sheet #1
		if (doubleSurplusFund == true && doubleUndividedProfits == false) {
			//Line 1: circulation bonds and double surplus fund. Circulation bonds/surplus fund can either be on line 1 or 2
			line3Or4Error = true;
			num = getNumLeftColumn(line1, 0, 5); 
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Circulation bonds: " + num);
				num = format(num);
				currentBank.setCirculationBonds(num);
				circulationBondsSet = true;
			}
			
			num = getNumRightColumn(line1, 45);
			
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Surplus fund: " + num);
				num = format(num);
				currentBank.setSurplusFund(num);
			}
			//Line 2: circulation bonds/deposit bonds and surplus fund. Deposit bonds can be on either line 3 or 4
			num = getNumLeftColumn(line2, 45, 60);
			
			if (!num.equals("") && !circulationBondsSet) {	
				num = num.replaceAll("\\s+","");
				System.out.println("Circulation bonds " + num);
				num = format(num);
				currentBank.setCirculationBonds(num);
			}
			else if (!num.equals("")){
				num = num.replaceAll("\\s+","");
				System.out.println("Deposit bonds " + num);
				num = format(num);
				currentBank.setDepositBonds(num);
			}
			else {
				if (num.equals("")) {
					System.out.println("Circulation bonds blank");
				}
			}
			
			num = getNumRightColumn(line2, 95);
				
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Surplus Fund: " + num);
				num = format(num);
				currentBank.setSurplusFund(num);
			}
			else {
				System.out.println("Surplus Fund Blank");
			}
			//Line 3: deposit bonds and undivided profits
			num = getNumLeftColumn(line3, 45, 60);
			
			if (!num.equals("") {
				num = num.replaceAll("\\s+","");
				System.out.println("Deposit bonds " + num);
				num = format(num);
				currentBank.setDepositBonds(num);
			}
			else {
					System.out.println("Deposit Bonds Blank");
			}
			
			num = getNumRightColumn(line3, 105);
				
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Undivided profits: " + num);
				num = format(num);
				currentBank.setUndividedProfits(num);
			}
			else {
				System.out.print("Undivided Profits Blank");
			}
		}
		//Case 2: see page 10 balance sheet 1
		else if (doubleSurplusFund == false && doubleUndividedProfits == true) {
			//Line 1****************************************************************
			line3Or4Error = true;
			num = getNumLeftColumn(line1, 50, 70);
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Circulation bonds: " + num);
				num = format(num);
				currentBank.setCirculationBonds(num);
				setCirculationBonds = true;
			}
			
			num = getNumRightColumn(line1, 110);
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Surplus fund: : " + num);
				num = format(num);
				currentBank.setSurplusFund(num);
			
			}
			else {
				System.out.println("Surplus fund blank");
			}
			//Line 2****************************************************************
			num = getNumLeftColumn(line2, 0, 5);
			
			if (!num.equals("") && !setCirculationBonds) {	
				num = num.replaceAll("\\s+","");
				System.out.println("Circulation bonds: " + num);
				num = format(num);
				currentBank.setCirculationBonds(num);
			}
			else if (!(num.equals(""))) {
				num = num.replaceAll("\\s+","");
				System.out.println("Deposit bonds " + num);
				num = format(num);
				currentBank.setDepositBonds(num);
			}
			else {
				if (circulationBonds.equals("")) {
					System.out.println("Circulation Bonds Blank");
				}
			}
			
			num = getNumRightColumn(line2, 45);
			
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Undivided profits: " + num);
				num = format(num);
				currentBank.setUndividedProfits(num);
				}
			}
			//Line 3****************************************************************
			num = getNumLeftColumn(line3, 50, 70);
			
			if (!num.equals("")) {
				num = num.replaceAll("\\s+","");
				System.out.println("Deposit bonds " + num);
				num = format(num);
				currentBank.setDepositBonds(num);
			}
			else {
				System.out.println("Deposit Bonds Blank");
			}
			
			num = getNumRightColumn(line3, 110);
			
			if (!num.equals("")) {		
				num = num.replaceAll("\\s+","");
				System.out.println("Undivided Profits " + num);
				num = format(num);
				currentBank.setUndividedProfits(num);
			}
			else {
				System.out.println("Undivided profits blank.");
			}
		}
		//Case 3: no errors
		else if (doubleSurplusFund == false && doubleUndividedProfits == false) {
			String[] data = doubleEntryLine(line1, "U.S. bonds to secure circulation", "Surplus fund");
			currentBank.setCirculationBonds(data[0]);
			currentBank.setSurplusFund(data[1]);
			
			data = doubleEntryLine(line2, "U.S. bonds to secure deposits", "Undivided profits");
			currentBank.setDepositBonds(data[0]);
			currentBank.setUndividedProfits(data[1]);
		}
	}
	
	/* Get # amount from line with entries in both columns.
	 * @param String line to be parsed, String item1 and String item2 representing names of fields 
	 * @return String[] with two indicies, with index 0 containing $ amnt of item 1 and index 1 containing the $ amnt of item 2 */
	public static String[] doubleEntryLine(String line, String item1, String item2) {
		line = line.trim();
		// Test if line is able to be parsed, else signal that there is an error
		if (lineIsValid(line) == false) {
			String[] data = new String[2];
			data[0] = "Error";
			return data;
		}
		if (startsWithNumber(line) == true) {
			String[] data = new String[2];
			data[1] = "Error2";
			return data;
		}
		//System.out.println(line);
		int firstDigitIndex = -1;
		int lastDigitIndex = -1;
		boolean error;
		
		// Get the first item
		String firstItemNumber = getNumLeftColumn(line, 50, 70);
		
		if (!firstItemNumber.equals("")) {		
			firstItemNumber = firstItemNumber.replaceAll("\\s+","");
			System.out.println(item1 + ": "  + firstItemNumber);
			firstItemNumber = format(firstItemNumber);
		}
		else {
			firstItemNumber = null;
			System.out.println("Something is wrong");
		}
		// Get the second item
		String secondItemNumber = getNumRightColumn(line, 110);
		
		if (!secondItemNumber.equals("")) {
			secondItemNumber = secondItemNumber.replaceAll("\\s+","");
			System.out.println(item2 + ": " + secondItemNumber);
			secondItemNumber = format(secondItemNumber);
			}
		}
		else {
			secondItemNumber = null;
			System.out.println(item2  + " " + "blank");
		}
		
		String[] data = new String[2];
		data[0] = firstItemNumber;
		data[1] = secondItemNumber;
		
		return data;
	}
	
	/* Get # amount from line with a single entry in the left column
	 * @param String line to be parsed, String item representing names of the field 
	 * @return String reprenting $ amnt of item */
	public static String singleEntryLine(String line, String item) {
		line = line.trim();
		int firstDigitIndex = -1;
		int lastDigitIndex = -1;
		boolean error;
		
		if (lineIsValid(line) == false) {
			return "Error";
		}
		if (startsWithNumber(line) == true) {
			return "Error2";
		}
		
		String itemNumber = getNumLeftColumn(line, 50, 70);
		
		if (!itemNumber.equals("")) {
			itemNumber = itemNumber.replaceAll("\\s+","");
			System.out.println(item + ": " + itemNumber);
			itemNumber = format(itemNumber);
		}
		
		return itemNumber;
	}
	
	/* Get last line of balance sheet containing total assets and total liabilities */
	public static void setTotal(String line) {
		line = line.trim();
		//System.out.println(line);
		int firstDigitIndex = -1;
		int lastDigitIndex = -1;
		boolean error;
		
		// Check if line can be parsed. If we are not on the last line something went wrong and we must abandon this bank
		if (checkTotalLine(line) == false) {
			System.out.println("Bank Abandoned");
			clearAndFindNextBank();
			return;
		}
		
		// Get total assets 
		String totalResources = getNumLeftColumn(line, 35, 60);
		
		if (!totalResources.equals("")) {
			totalResources = totalResources.replaceAll("\\s+","");
			System.out.println("Total resources" + ": "  + totalResources);
			totalResources = format(totalResources);
			currentBank.setTotalResources(totalResources);
			}
		}
		else {
			System.out.println("Something is wrong");
		}
		// Get total liabilities
		String totalLiabilities = getNumRightColumn(line, 105);
		
		if (totalLiabilities.equals("")) {		
			totalLiabilities = totalLiabilities.replaceAll("\\s+","");
			System.out.println("Total liabilities" + ": " + totalLiabilities);
			totalLiabilities = format(totalLiabilities);
			currentBank.setTotalLiabilities(totalLiabilities);
		}
		else {
			System.out.println("Total liabilities"  + " " + "blank");
		}
		
		if (totalLiabilities == null && totalResources != null) {
			currentBank.setTotalLiabilities(totalResources);
		}
		else if (totalLiabilities != null && totalResources == null) {
			currentBank.setTotalResources(totalLiabilities);
		}
		
		if (totalLiabilities != null && totalResources != null && totalLiabilities.length() > totalResources.length()) {
			currentBank.setTotalResources(totalLiabilities);
		}
		else if (totalLiabilities != null && totalResources != null && totalLiabilities.length() < totalResources.length()) {
			currentBank.setTotalLiabilities(totalResources);
		}
	}
	
	/* Get number in left column of line
	 * @param String line to be parsed, int startPos indicating index where to start looking for the number, and int endPos where to stop looking for the number 
	 * @return Sring representing the number is there it was found, or an error if the number could not be retrieved */
	public static String getNumLeftColumn(String line, int startPos, int endPos) {
		String num = "";
		int firstDigit = -1;
		int lastDigit = -1;
		error = false;
		//Get first and last index of number
		if (line.length() > startPos) {
			for (int i = startPos; i < endPos && i < line.length(); i++) {
				if (Character.isDigit(line.charAt(i))) {
					firstDigit = i;
					break;
				}
			}
			for (int i = firstDigit + 1; i < firstDigit + 20 && i < line.length(); i++) {
				if (Character.isDigit(line.charAt(i))) {
					lastDigit = i;
				}
			}
			// If first and last index were found, construct the string, indicating if there is an error
			if (! (lastDigit == -1 || firstDigit == -1)) {
				for (int i = firstDigit; i <= lastDigit; i++) {
					num = num + line.charAt(i);
				}
				
				if (lastDigit + 1 < line.length() && Character.compare(line.charAt(lastDigit + 1), '.') == 0) {
					num = num + ".";
				}
				// indicate that there is an error if we have a comma instead of a period
				else if (lastDigit + 1 < line.length() && Character.compare(line.charAt(lastDigit + 1), ',') == 0) {
					error = true;
				}
				// Indicate that there is an error if there are too many spaces
				if (spaceCounter(num) >= 4) {
					error = true;
				}
				//If there is text before our number, indicate that there is an error
				if (firstDigit - 4 > 0) {
					for (int i = firstDigit; i > firstDigit - 4 && i >= 0; i--) {
						if(Character.isLetter(line.charAt(i)) || Character.compare(line.charAt(i), ',') == 0) {
							error = true;
						}
					}
				}
			}
		}
		
		return num;
	
	}
	
	/* Get number in right column of line
	 * @param String line to be parsed, int startPos indicating index where to start looking for the number
	 * @return Sring representing the number is there it was found, or an error if the number could not be retrieved */
	public static String getNumRightColumn(String line, int startPos) {
		String num ="";
		error = false;
		
		if (line.length() > startPos) {
			firstDigit = -1;
			lastDigit = -1;
			//Get first and last index of number
			for (int i = startPos; i < line.length(); i++) {
				if (Character.isDigit(line.charAt(i))) {
					firstDigit = i;
					break;
				}
			}
			for (int i = firstDigit + 1; i < line.length(); i++) {
				if (Character.isDigit(line.charAt(i))) {
					lastDigit = i;
				}
			}
			// If first and last index were found, construct the string, indicating if there is an error
			if (! (lastDigit == -1 || firstDigit == -1)) {
				for (int i = firstDigit; i <= lastDigit; i++) {
					num = num + line.charAt(i);
				}
					
				if (lastDigit + 1 < line.length() && Character.compare(line.charAt(lastDigit + 1), '.') == 0) {
					num = num + ".";
				}
				// indicate that there is an error if we have a comma instead of a period
				else if (lastDigit + 1 < line.length() && Character.compare(line.charAt(lastDigit + 1), ',') == 0) {
					error = true;
				}
				// Indicate that there is an error if there are too many spaces
				if (spaceCounter(num) >= 4) {
					error = true;
				}
				//If there is text before our number, indicate that there is an error
				if (firstDigit - 4 > 0) {
					for (int i = firstDigit; i > firstDigit - 4 && i >= 0; i--) {
						if(Character.isLetter(line.charAt(i)) || Character.compare(line.charAt(i), ',') == 0) {
							error = true;
						}
					}
				}
			}
		}
		
		return num;
	}
	
	/* Check whether the we are at the last line of the balance sheet
	 * @param line to be parsed
	 * true if we are at the last line, false otherwise */
	public static boolean checkTotalLine(String line) {
		line = line.replaceAll("\\s+","");
		for (int i = 0; i <= line.length() - 3; i++) {
			if (line.substring(i, i + 3).toLowerCase().equals("tot")) {
				return true;
			}
		}
		return false;
	}
	
	// Erase all the data in this bank and move to the next bank. Called when errors make balance sheet impossible to read
	public static void clearAndFindNextBank() {
		currentBank = null;
		String nextLine = null;
		while (input.hasNextLine()) {
			nextLine = input.nextLine();
			if (checkTotalLine(nextLine) == true) {
				return;
			}
			if (nextLine.contains("Digitized")) {
				newPage = true;
				return;
			}
		}
	}
	
	/* Tests where a line can be parsed, or if it is a faulty line that does not contain any information 
	 * @param line to be parsed
	 * @return true if the line contains useful information, false otherwise */
	public static boolean lineIsValid(String line) {
		line = line.trim();
		int numLetters = 0;
		int numSpaces = 0;
		int numDigits =  0;
		//Tests whether the line is long enough to contain a number
		boolean validLine = true;
		if (line.length() < 9) {
			return false;
		}
		//Test whether line begins with some text, indicating that a balance sheet field label is there
		for (int i = 0; i < 7; i++) {
			if (Character.isLetter(line.charAt(i))) {
				numLetters++;
			}
			if (line.substring(i, i + 1).equals(" ")) {
				numSpaces++;
			}
			if (Character.isDigit(line.charAt(i))) {
				numDigits++;
			}
		}
		if (numLetters < 2 && numDigits < 2) {
			validLine = false;
		}
		if (validLine == false) {
			System.out.println("Line Skip");
		}
		return validLine;	
	}
	
	/* Indicate whether a line starts with a number.
	 * @param line to be parsed
	 * @return true if the line starts with a number indicating that we must skip two lines, false otherwise */
	public static boolean startsWithNumber(String line) {
		line = line.trim();
		int numDigits =  0;
		int numCommas = 0;
		boolean number = false;
		// true if line is too short to contain field label and number 
		if (line.length() < 7) {
			return true;
		}
		// true if line starts with 3 or more digits/commas
		for (int i = 0; i < 7; i++) {
			if (Character.isDigit(line.charAt(i))) {
				numDigits++;
			}
			if (line.substring(i, i + 1).equals(",")) {
				numCommas++;
			}
		}
		if (numDigits + numCommas >= 3) {
			number = true;
		}
		if (number == true) {
			System.out.println("Double line Skip");
		}
		return number;	
	}
	
	// Create each column in our excel file. The first columns represent header information, and the rest respresents a field in the balance sheet
	public static void createExcelFile() {
            workbook = new HSSFWorkbook();
            sheet = workbook.createSheet("FirstSheet");  

            HSSFRow rowhead = sheet.createRow((short)0);
            rowhead.createCell(0).setCellValue("State");
            rowhead.createCell(1).setCellValue("Bank Name, City");
            rowhead.createCell(2).setCellValue("Bank Number");
            rowhead.createCell(3).setCellValue("Loans and Discounts");
            rowhead.createCell(4).setCellValue("Overdrafts");
            rowhead.createCell(5).setCellValue("U.S.bonds to secure circulation");
            rowhead.createCell(6).setCellValue("U.S. bonds to secure deposits");
            rowhead.createCell(7).setCellValue("U.S. bonds on hand");
            rowhead.createCell(8).setCellValue("Stocks, securities, etc.");
            rowhead.createCell(9).setCellValue("Due from approved reserve agents");
            rowhead.createCell(10).setCellValue("Due from other national banks");
            rowhead.createCell(11).setCellValue("Due from state banks and bankers");
            rowhead.createCell(12).setCellValue("Bank'g house, furniture, and fixtures");
            rowhead.createCell(13).setCellValue("Other real estate and mortg's owed");
            rowhead.createCell(14).setCellValue("Current expenses and taxes paid");
            rowhead.createCell(15).setCellValue("Premiums on U.S. bonds");
            rowhead.createCell(16).setCellValue("Checks and other cash items");
            rowhead.createCell(17).setCellValue("Exchanges for clearing house");
            rowhead.createCell(18).setCellValue("Bills of other national banks");
            rowhead.createCell(19).setCellValue("Fractional currency, nickels, cents");
            rowhead.createCell(20).setCellValue("Specie");
            rowhead.createCell(21).setCellValue("Legal-tender notes");
            rowhead.createCell(22).setCellValue("U.S. certificates of deposits");
            rowhead.createCell(23).setCellValue("Redemption fund with Treas.");
            rowhead.createCell(24).setCellValue("Due from Treasurer U.S.");
            rowhead.createCell(25).setCellValue("Total Assets");
            rowhead.createCell(26).setCellValue("Capital stock paid in");
            rowhead.createCell(27).setCellValue("Surplus fund");
            rowhead.createCell(28).setCellValue("Undivided profits");
            rowhead.createCell(29).setCellValue("National-bank notes outstanding");
            rowhead.createCell(30).setCellValue("State-bank notes outstanding");
            rowhead.createCell(31).setCellValue("Dividends unpaid");
            rowhead.createCell(32).setCellValue("Individual deposits");
            rowhead.createCell(33).setCellValue("Certified checks");
            rowhead.createCell(34).setCellValue("United States deposits");
            rowhead.createCell(35).setCellValue("Deposits of U.S. disbursing officers");
            rowhead.createCell(36).setCellValue("Due to other national banks");
            rowhead.createCell(37).setCellValue("Due to State banks and bankers");
            rowhead.createCell(38).setCellValue("Notes and bilss rediscounted");
            rowhead.createCell(39).setCellValue("Bills payable");
            rowhead.createCell(40).setCellValue("Total liabilities");
            rowhead.createCell(41).setCellValue("Page Number");
	}
	
	// Create a new row with information for the current bank. Called once we have extracted all the data in the balance sheet into a Bank object
	public static void bankToExcel() {
		HSSFRow row = sheet.createRow((short)excelRowCounter);
        row.createCell(0).setCellValue(currentBank.getState());
        row.createCell(1).setCellValue(currentBank.getName() + ", " + currentBank.getCity());
        row.createCell(2).setCellValue(currentBank.getNumber());
        row.createCell(3).setCellValue(currentBank.getLoansDiscounts());
        row.createCell(4).setCellValue(currentBank.getOverdrafts());
        row.createCell(5).setCellValue(currentBank.getCirculationBonds());
        row.createCell(6).setCellValue(currentBank.getDepositBonds());
        row.createCell(7).setCellValue(currentBank.getBondsOnHand());
        row.createCell(8).setCellValue(currentBank.getStocksSecurities());
        row.createCell(9).setCellValue(currentBank.getReserveAgents());
        row.createCell(10).setCellValue(currentBank.getNationalBanks());
        row.createCell(11).setCellValue(currentBank.getStateBanks());
        row.createCell(12).setCellValue(currentBank.getPhysicalAssets());
        row.createCell(13).setCellValue(currentBank.getRealEstate());
        row.createCell(14).setCellValue(currentBank.getExpensesAndTaxes());
        row.createCell(15).setCellValue(currentBank.getBondPremiums());
        row.createCell(16).setCellValue(currentBank.getCashItems());
        row.createCell(17).setCellValue(currentBank.getClearingHouse());
        row.createCell(18).setCellValue(currentBank.getOtherNationalBanks());
        row.createCell(19).setCellValue(currentBank.getFractionalCurrency());
        row.createCell(20).setCellValue(currentBank.getSpecie());
        row.createCell(21).setCellValue(currentBank.getLegalTenderNotes());
        row.createCell(22).setCellValue(currentBank.getDepositCertificates());
        row.createCell(23).setCellValue(currentBank.getRedemptionFunds());
        row.createCell(24).setCellValue(currentBank.getTreasurerDues());
        row.createCell(25).setCellValue(currentBank.getTotalResources());
        row.createCell(26).setCellValue(currentBank.getCapitalStock());
        row.createCell(27).setCellValue(currentBank.getSurplusFund());
        row.createCell(28).setCellValue(currentBank.getUndividedProfits());
        row.createCell(29).setCellValue(currentBank.getNationalBankNotes());
        row.createCell(30).setCellValue(currentBank.getStateBankNotes());
        row.createCell(31).setCellValue(currentBank.getDividensUnpaid());
        row.createCell(32).setCellValue(currentBank.getIndividualDeposits());
        row.createCell(33).setCellValue(currentBank.getCertifiedChecks());
        row.createCell(34).setCellValue(currentBank.getUnitedStatesDeposits());
        row.createCell(35).setCellValue(currentBank.getDisbursingDeposits());
        row.createCell(36).setCellValue(currentBank.getDueToNationalBanks());
        row.createCell(37).setCellValue(currentBank.getDueToStateBanks());
        row.createCell(38).setCellValue(currentBank.getRediscounted());
        row.createCell(39).setCellValue(currentBank.getPayable());
        row.createCell(40).setCellValue(currentBank.getTotalLiabilities());
        row.createCell(41).setCellValue(page);
        
        excelRowCounter++;
	}
	
	// Output excel sheet in folder where project is located
	public static void finalizeExcelSheet() {
		try {
			FileOutputStream fileOut = new FileOutputStream("FullDocToExcel.xls");
	        workbook.write(fileOut);
	        fileOut.close();
	        System.out.println("Your excel file has been generated!");
		}
	    catch ( Exception ex ) {
	    	 System.out.println(ex);
	    }
        
	}
	
	/* If the number is not properly formatted, try to format it properly. Otherwise, indicate the number is an error
	 * @param String number to format
	 * @return properly formatted number, "Error" if number cannot be made sense of */
	public static String format(String number) {
		// Test if we have already detected that the number is an error
		if (number.length() < 4 || !Character.isDigit(number.charAt(0)) || 
				Character.compare(number.charAt(0), '0') == 0 || error) {
			error = false;
			return "Error";
		}
		//If the number is not formatted properly, try to fix it
		if (wrongNumberFormat(number)) {
			for (int i = 0; i < number.length(); i++) {
				if (!(Character.isDigit(number.charAt(i)) || Character.compare(number.charAt(i), ',') == 0 ||
						Character.compare(number.charAt(i), '.') == 0)) {
					// Replace the letter "l" with the number "1"
					if (Character.compare(number.charAt(i), 'l') == 0) {
						number = number.substring(0, i) + "1" + number.substring(i + 1, number.length()); 
					}
					// Replace the letter "O" with the number "0"
					else if (Character.compare(number.charAt(i), 'O') == 0) {
						number = number.substring(0, i) + "0" + number.substring(i + 1, number.length());
					}
					// Return error if there is another letter in the number
					else if ( i < number.length() - 2 && Character.isLetter(number.charAt(i))) {
						System.out.println("Error2");
						error = false;
						return "Error";
					}
					// If there is random spaces/punctuation, remove it from the number
					else {
						number = number.substring(0, i) + number.substring(i + 1, number.length());
						i--;
					}
				}
			}
			
			// try to find the "."
			boolean dotDetected = false;
			for (int i = number.length() - 1; i >= number.length() - 3; i--) {
				if (Character.compare(number.charAt(i), ',') == 0) {
					number = number.substring(0, i) + "." + number.substring(i + 1, number.length());
				}
				if (Character.compare(number.charAt(i), '.') == 0) {
					dotDetected = true;
				}
			}
			// Add a "." if not was detected
			if (dotDetected == false) {
				number = number.substring(0, number.length() - 2) + "." + number.substring(number.length() - 2, number.length());
			}
			
			//If the number ends with a "."
			if (Character.compare(number.charAt(number.length() - 1), '.') == 0) {
				//If there is another "." earlier in the number, return error
				if (number.length() - 3 > 0 && Character.compare(number.charAt(number.length() - 3), '.') == 0 ||
						number.length() - 4 > 0 && Character.compare(number.charAt(number.length() - 4), '.') == 0) {
					error = false;
					return "Error";
				}
				else {
					// Else remove the decimal and add 2 zeros after the dot
					number = number + "00";
				}
			}
			//If there is one decimal place after the "."
			else if (Character.compare(number.charAt(number.length() - 2), '.') == 0) {
				//If there is another "." earlier in the number, return error
				if (number.length() - 4 > 0 && Character.compare(number.charAt(number.length() - 4), '.') == 0) {
					return "Error";
				}
				else {
					// Else remove the decimal and add 1 zeros after the dot
					if (Character.compare(number.charAt(number.length() - 3), '.') == 0) {
						number = number.substring(0, number.length() - 3) + number.substring(number.length() - 2, number.length());  
					}
					number = number + "0";
				}
			}
			
			//Remove dots four places before the end of the number (commas are often incorrectly represented as dots)
			for (int i = number.length() - 4; i >= 0; i--) {
				if (Character.compare(number.charAt(i), '.') == 0) {
					number = number.substring(0, i) + number.substring(i + 1, number.length());
					i--;
				}
			}
			
			//Add commas after every 3 numbers
			for (int i = number.length() - 7; i >= 0; i = i - 4) {
				if (!(Character.compare(number.charAt(i), ',') == 0)) {
					number = number.substring(0, i + 1) + "," + number.substring(i + 1, number.length());
					i++;
				}
			}
		}
		
		//If the number is still in an incorrect format, return error. Else return the number
		if (wrongNumberFormat(number)) {
			error = false;
			return "Error";
		}
		else {
			error = false;
			return number;
		}
	}
	
	/*Ensure that the number is in the following format: 111,222,333.45
	 * @param String number to be tested
	 * @return true if number is not formatted correctly. False otherwise */
	public static boolean wrongNumberFormat(String number) {
		if (!(Character.isDigit(number.charAt(number.length() - 1)) && Character.isDigit(number.charAt(number.length() - 2)) && 
				Character.compare(number.charAt(number.length() - 3), '.') == 0)) {
			return true;
		}
		for (int i = number.length() - 4; i >= 0; i = i - 4) {
			if (i >= 0 && !Character.isDigit(number.charAt(i))) {
				return true;
			}
			if ((i - 1) >= 0 && !Character.isDigit(number.charAt(i - 1))) {
				return true;
			}
			if (i - 2 >= 0 && !Character.isDigit(number.charAt(i - 2))) {
				return true;
			}
			if (i - 3 >= 0 && !(Character.compare(number.charAt(i - 3), ',') == 0)) {
				return true;
			}
		}
		
		return false;
	}
	
	/* Count the number of spaces in a number. Too many spaces indicate that there is an issue with this number 
	 * @param number to be parsed 
	 * @return number of blank spaces in the number */
	public static int spaceCounter(String number) {
		int spaceCounter = 0;
		int maxSpaces = 0;
		for (int i  = 0; i < number.length(); i++) {
			if (!(number.substring(i, i + 1).equals(" "))) {
				if (spaceCounter > maxSpaces) {
				maxSpaces = spaceCounter;
				}
			spaceCounter = 0;
			} else {
				spaceCounter++;
			}
		}
		
		return maxSpaces;
	}
	
	/* Find the page number in the first line of a page 
	 * @param String line containing the number of a page
	 * @return String representing the number of a page */
	public static void setPageNumber(String line) {
		String pageNumber = "";
		for (int i = 0; i < line.length(); i++) {
			if (Character.isDigit(line.charAt(i))) {
				pageNumber = pageNumber + line.substring(i, i + 1);
			}
		}
		
		page = pageNumber;
	}
}

