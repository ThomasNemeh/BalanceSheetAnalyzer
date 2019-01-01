/*Class to contain fields associated with each bank balance sheet. Author: Thomas Nemeh. Summer 2017*/

public class Bank {
	
	private String name;
	
	private String city;
	
	private String number;
	
	private String state;
	
	// Resources
	private String LoansDiscounts;		//line 1   *(of balance sheet, noted for later)
	private String Overdrafts;			//line 2	
	private String circulationBonds;	//line 3	
	private String depositBonds;		//line 4	
	private String bondsOnHand;			//line 5	
	private String stocksSecurities;	//line 6	
	private String reserveAgents;		//line 7	
	private String nationalBanks;		//line 8	
	private String stateBanks;			//line 9	
	private String physicalAssets;		//line 10	
	private String realEstate;			//line 11	
	private String expensesAndTaxes;	//line 12	
	private String bondPremiums;		//line 13	
	private String cashItems;			//line 14	
	private String clearingHouse;		//line 15	
	private String otherNationalBanks;	//line 16	
	private String fractionalCurrency;	//line 17	
	private String specie;				//line 18	
	private String legalTenderNotes;	//line 19	
	private String depositCertificates;	//line 20	
	private String redemptionFunds;		//line 21	
	private String TreasurerDues;		//line 22	
	private String totalResources; 		//line 24*	
		
	//Liabilities
	private String capitalStock;		//line 1
	private String surplusFund;			//line 3
	private String undividedProfits;	//line 4
	private String nationalBankNotes;	//line 6
	private String stateBankNotes;		//line 7
	private String dividensUnpaid;		//line 9
	private String individualDeposits;	//line 11
	private String certifiedChecks;		//line 12 (if listed)
	private String unitedStatesDeposits;//line 12 or 13
	private String disbursingDeposits;	//line 13 or 14
	private String dueToNationalBanks;	//line 15 or 16
	private String dueToStateBanks;		//line 16 or 17
	private String rediscounted;		//line 18 or 19
	private String payable;				//line 19 or 20
	private String totalLiabilities;	//line 24
	
	public Bank() {
		name = null;
		
		city = null;
		
		number = null;
		
		state = null;
		
		LoansDiscounts = null;    
		Overdrafts = null;
		circulationBonds = null;
		depositBonds = null;
		bondsOnHand = null;
		stocksSecurities = null;
		reserveAgents = null;
		nationalBanks = null;
		stateBanks = null;
		physicalAssets = null;
		realEstate = null;
		expensesAndTaxes = null;
	    bondPremiums = null;
	    cashItems = null;
	    clearingHouse = null;
	    otherNationalBanks = null;
		fractionalCurrency = null;
		specie = null;
		legalTenderNotes = null;
		depositCertificates = null;
		redemptionFunds = null;
		TreasurerDues = null;
		totalResources  = null;
		 
		capitalStock = null;
		surplusFund = null;
		undividedProfits = null;
		nationalBankNotes = null;
		stateBankNotes = null;
		dividensUnpaid = null;
		individualDeposits = null;
		certifiedChecks = null;
		unitedStatesDeposits = null;
		disbursingDeposits = null;
		dueToNationalBanks = null;
		dueToStateBanks = null;
		rediscounted = null;
		payable = null;
		totalLiabilities = null;
	}
	
	//Accessor general information**********
	public String getName() {
		return name;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}
	
	public String getNumber() {
		return number;
	}
	
	//Accessor methods resources**********
	public String getLoansDiscounts() {
		return LoansDiscounts;
	}
	
	public String getOverdrafts() {
		return Overdrafts;
	}
	
	public String getCirculationBonds() {
		return circulationBonds;
	}
	
	public String getDepositBonds() {
		return depositBonds;
	}
	
	public String getBondsOnHand() {
		return bondsOnHand;
	}
	
	public String getStocksSecurities() {
		return stocksSecurities;
	}
	
	public String getReserveAgents() {
		return reserveAgents;
	}
	
	public String getNationalBanks() {
		return nationalBanks;
	}
	
	public String getStateBanks() {
		return stateBanks;
	}
	
	public String getPhysicalAssets() {
		return physicalAssets;
	}
	
	public String getRealEstate() {
		return realEstate;
	}
	
	public String getExpensesAndTaxes() {
		return expensesAndTaxes;
	}
	
	public String getBondPremiums() {
		return bondPremiums;
	}
	
	public String getCashItems() {
		return cashItems;
	}
	
	public String getClearingHouse() {
		return clearingHouse;
	}
	
	public String getOtherNationalBanks() {
		return otherNationalBanks;
	}
	
	public String getFractionalCurrency() {
		return fractionalCurrency;
	}
	
	public String getSpecie() {
		return specie;
	}
	
	public String getLegalTenderNotes() {
		return legalTenderNotes;
	}
	
	public String getDepositCertificates() {
		return depositCertificates;
	}
	
	public String getRedemptionFunds() {
		return redemptionFunds;
	}
	
	public String getTreasurerDues() {
		return TreasurerDues;
	}
	
	public String getTotalResources() {
		return totalResources;
	}
	
	//Accessor methods liabilities**********
	public String getCapitalStock() {
		return capitalStock;
	}
	
	public String getSurplusFund() {
		return surplusFund;
	}
	
	public String getUndividedProfits() {
		return undividedProfits;
	}
	
	public String getNationalBankNotes() {
		return nationalBankNotes;
	}
	
	public String getStateBankNotes() {
		return stateBankNotes;
	}
	
	public String getDividensUnpaid() {
		return dividensUnpaid;
	}
	
	public String getIndividualDeposits() {
		return individualDeposits;
	}
	
	public String getCertifiedChecks() {
		return certifiedChecks;
	}
	
	public String getUnitedStatesDeposits() {
		return unitedStatesDeposits;
	}
	
	public String getDisbursingDeposits() {
		return disbursingDeposits;
	}
	
	public String getDueToNationalBanks() {
		return dueToNationalBanks;
	}
	
	public String getDueToStateBanks() {
		return dueToStateBanks;
	}
	
	public String getRediscounted() {
		return rediscounted;
	}
			
	public String getPayable() {
		return payable;
	}
	
	public String getTotalLiabilities() {
		return totalLiabilities;
	}
	
	//Setter Methods general information**********
	public void setName(String x) {
		name = x;
	}
	
	public void setCity(String x) {
		city = x;
	}
	
	public void setState(String x) {
		state = x;
	}
	
	public void setNumber(String x) {
		number = x;
	}
	
	//Setter Methods Assets**********
	public void setLoansDiscounts(String x) {
		 LoansDiscounts = x;
	}
	
	public void setOverdrafts(String x) {
		 Overdrafts = x;
	}
	
	public void setCirculationBonds(String x) {
		 circulationBonds = x;
	}
	
	public void setDepositBonds(String x) {
		 depositBonds = x;
	}
	
	public void setBondsOnHand(String x) {
		 bondsOnHand = x;
	}
	
	public void setStocksSecurities(String x) {
		 stocksSecurities = x;
	}
	
	public void setReserveAgents(String x) {
		 reserveAgents = x;
	}
	
	public void setNationalBanks(String x) {
		 nationalBanks = x;
	}
	
	public void setStateBanks(String x) {
		 stateBanks = x;
	}
	
	public void setPhysicalAssets(String x) {
		 physicalAssets = x;
	}
	
	public void setRealEstate(String x) {
		 realEstate = x;
	}
	
	public void setExpensesAndTaxes(String x) {
		 expensesAndTaxes = x;
	}
	
	public void setBondPremiums(String x) {
		 bondPremiums = x;
	}
	
	public void setCashItems(String x) {
		 cashItems = x;
	}
	
	public void setClearingHouse(String x) {
		 clearingHouse = x;
	}
	
	public void setOtherNationalBanks(String x) {
		 otherNationalBanks = x;
	}
	
	public void setFractionalCurrency(String x) {
		 fractionalCurrency = x;
	}
	
	public void setSpecie(String x) {
		 specie = x;
	}
	
	public void setLegalTenderNotes(String x) {
		 legalTenderNotes = x;
	}
	
	public void setDepositCertificates(String x) {
		 depositCertificates = x;
	}
	
	public void setRedemptionFunds(String x) {
		 redemptionFunds = x;
	}
	
	public void setTreasurerDues(String x) {
		 TreasurerDues = x;
	}
	
	public void setTotalResources(String x) {
		 totalResources = x;
	}
	
	//Setter methods liabilities
	public void setCapitalStock(String x) {
		 capitalStock= x;
	}
	
	public void setSurplusFund(String x) {
		 surplusFund= x;
	}
	
	public void setUndividedProfits(String x) {
		 undividedProfits= x;
	}
	
	public void setNationalBankNotes(String x) {
		 nationalBankNotes= x;
	}
	
	public void setStateBankNotes(String x) {
		 stateBankNotes= x;
	}
	
	public void setDividensUnpaid(String x) {
		 dividensUnpaid= x;
	}
	
	public void setIndividualDeposits(String x) {
		 individualDeposits= x;
	}
	
	public void setCertifiedChecks(String x) {
		 certifiedChecks= x;
	}
	
	public void setUnitedStatesDeposits(String x) {
		 unitedStatesDeposits= x;
	}
	
	public void setDisbursingDeposits(String x) {
		 disbursingDeposits= x;
	}
	
	public void setDueToNationalBanks(String x) {
		 dueToNationalBanks= x;
	}
	
	public void setDueToStateBanks(String x) {
		 dueToStateBanks= x;
	}
	
	public void setRediscounted(String x) {
		 rediscounted= x;
	}
			
	public void setPayable(String x) {
		 payable= x;
	}
	
	public void setTotalLiabilities(String x) {
		 totalLiabilities= x;
	}
	
	public void clear() {
		LoansDiscounts = null;    
		Overdrafts = null;
		circulationBonds = null;
		depositBonds = null;
		bondsOnHand = null;
		stocksSecurities = null;
		reserveAgents = null;
		nationalBanks = null;
		stateBanks = null;
		physicalAssets = null;
		realEstate = null;
		expensesAndTaxes = null;
	    bondPremiums = null;
	    cashItems = null;
	    clearingHouse = null;
	    otherNationalBanks = null;
		fractionalCurrency = null;
		specie = null;
		legalTenderNotes = null;
		depositCertificates = null;
		redemptionFunds = null;
		TreasurerDues = null;
		totalResources  = null;
		 
		capitalStock = null;
		surplusFund = null;
		undividedProfits = null;
		nationalBankNotes = null;
		stateBankNotes = null;
		dividensUnpaid = null;
		individualDeposits = null;
		certifiedChecks = null;
		unitedStatesDeposits = null;
		disbursingDeposits = null;
		dueToNationalBanks = null;
		dueToStateBanks = null;
		rediscounted = null;
		payable = null;
		totalLiabilities = null;
		
		name = "Bank Error: " + name;
	}
}
