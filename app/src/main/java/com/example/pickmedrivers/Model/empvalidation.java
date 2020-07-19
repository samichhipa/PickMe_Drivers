package com.example.pickmedrivers.Model;

public class empvalidation
{
	   // validate first name
	   public static boolean validateName( String firstName )
	   {
	      return firstName.isEmpty();
	   } // end method validateFirstName

	     
	   // validate last name
	   public static boolean validateLastName( String lastName )
	   {
		   return !lastName.isEmpty();
	   } // end method validateLastName

	   
	   // validate zip
	   public static boolean validateZip(String zip )
	   {
		   //d+
	      return zip.matches("\\d+");
	   } // end method validateZip

	   // validate phone
	   public static boolean validateNIC( String nic )
	   {
	      return nic.matches( "\\d{5}-\\d{7}-\\d{1}" );
	   } // end method validatePhone
	
	   // validate phone
	   public static boolean validateEMAIL(String email)
	   {
		   //s+@\s+\.\s+  mail@yahoo.com
	      return email.matches( "\\S+@\\S+\\.\\S+" );
		  // return email.matches( "[a-zA-Z]" );
	   } // end method validatePhone
	
	   public static boolean validatePhoneNo(String phno)
	   {
		   //ph no like +923214123135
	      return phno.matches( "\\S+\\d{12}" );
		 
	   } // end method validatePhone

	   public static boolean validatedob(String dob)
	   {
		   //dob like 2/2/1979
	      return dob.matches( "\\d{1,2}/\\d{1,2}/\\d{2,4}" );
		 
	   } // end method validatePhone
	   
	   public static boolean validatepwd(String pwd)
	   {
		   //4-10 character password that starts with a letter.
	      return pwd.matches( "\\S[a-zA-Z]\\S{3,9}" );
		  
	   } // end method validatePhone
	   
	 
}
