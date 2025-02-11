package com.telus.credit.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ID_ValidationUtils {
	  public static final String NON_ALPHANUMERIC_CHAR ="[^\\ a-zA-Z0-9]+";
	  private static final Pattern NON_ALPHANUMERIC_CHAR_REGEX = Pattern.compile(NON_ALPHANUMERIC_CHAR);
	  private static final Pattern SPACE_REGEX = Pattern.compile(" ");  
	  public static final java.lang.String Subset_NON_ALPHANUMERIC_CHAR = "[^\\ \\-\\_a-zA-Z0-9]+";
	  private static final Pattern INVALID_CHAR_REGEX = Pattern.compile(Subset_NON_ALPHANUMERIC_CHAR);


	  
	  public static boolean isValid_ID_Number(String aNumberStr)
	  {
	      if ( aNumberStr == null  || aNumberStr.length()==0 ) {
	          return false;
	      }
	      Matcher aMatcher = INVALID_CHAR_REGEX.matcher(aNumberStr);
	      if( aMatcher!= null && aMatcher.find()){
	    	  return false;
	      }
	      aNumberStr = INVALID_CHAR_REGEX.matcher(aNumberStr).replaceAll("");

	    	  
	      aNumberStr = NON_ALPHANUMERIC_CHAR_REGEX.matcher(aNumberStr).replaceAll("");
	      aNumberStr = SPACE_REGEX.matcher(aNumberStr).replaceAll("");
	      
	      if (aNumberStr.length()==0 ) {
	          return false;
	      }      
	      try{
		      Integer aNumberInteger= new Integer(aNumberStr);
		      if(aNumberInteger.intValue()==0) {
		    	  return false;
		      }
	      }
	      catch(Throwable e){}
	      
	      return true;
	  }


  /**
   * <p><b>Description</b> Validates SIN number. </p>
   * <p><b>Algorythm for SIN validation: </b></p>
   * <ol>
   * 		<li> Write the SIN on a sheet of paper, e.g. 440-968-592</li>
   *		<li> Insert a check mark over the 2nd, 4th, 6th and 8th digits</li> 
   *      <li> 
   * 			Write the SIN again, but this time doubling the digits that were check-marked, 
   * 			i.e. 480-18616-5182. 
   * 		</li> 
   *		<li> Where the doubling of a single digit results in a two-digit number, then: 
   *			<ol>
   *				<li> Add these two digits to form a single digit</li> 
   *				<li> Add all of these numbers, i.e. 4+8+0+9+6+7+5+9+2 = 50.</li>
   *			</ol>
   *		</li>
   *		<li>
   *			If the SIN is valid the resulting total must be of a multiple of ten. 
   *			Therefore the above SIN is valid in that the total is 50.
   *		</li>
   * </ol>
   * @param sinNumberStr SIN number.
   * @return true if SIN number is valid; false otherwise
   */
  public static boolean isValid_SIN_Number(String sinNumberStr)
  {
      if ( sinNumberStr == null || !isSinInCorrectFormat( sinNumberStr ) ) {
          return false;
      }
      int[] digitsInSinNumber = new int[9];

      fill( digitsInSinNumber, sinNumberStr );

      int sumOfDigits = 0;

      for ( int i = 0; i < 9; i++ ) {
          if ( i == 1 || i == 3 || i == 5 || i == 7 ) {
              sumOfDigits += convertAndAddNumber( digitsInSinNumber[i] );
          }
          else {
              sumOfDigits += digitsInSinNumber[i];
          }
      }
      return sumOfDigits % 10 == 0 ? true : false;
  }
  protected static int convertAndAddNumber(int num)
  {
      int result = 0;
      int product = num * 2;
      int[] digits = new int[2];
      if ( product > 9 ) {
          fill( digits, product );
          result = addNumbersInArrayOfSize2( digits );
      }
      else {
          result = product;
      }
      return result;
  }
  protected static int addNumbersInArrayOfSize2(int[] nums)
  {
      return nums[0] + nums[1];
  }
  
  protected static boolean isSinInCorrectFormat(String num)
  {
      boolean result = true;
      if ( num.length() < 9 ) {
          result = false;
      }

      try {
          Integer.parseInt( num );
      }
      catch ( NumberFormatException e1 ) {
          result = false;
      }

      return result;
  }

  private static void fill(int[] digits, int num)
  {
      for ( int i = 0; i < digits.length; i++ ) {
          digits[i] = getDigit( num, i, digits.length );
      }
  }

	
	    protected static void fill(int[] digits, String str)
  {
      for ( int i = 0; i < digits.length; i++ ) {
          digits[i] = Character.getNumericValue( str.charAt( i ) );
      }
  }
  protected static int getDigit(int num, int position, int numOfDigits)
  {
      String numString = String.valueOf( num );
      if ( numString.length() > numOfDigits ) {
          throw new IllegalArgumentException(
                  "Exception in CreditIDCard.getDigit(): num - " + num
                          + "; position - " + position );
      }
      return Character.getNumericValue( numString.charAt( position ) );

  }


}