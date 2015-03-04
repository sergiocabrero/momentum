package utils;

/**
 * ExceptionManager class manages all the exceptions in the system. All the catched exceptions must be sent to this class 
 *  
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */
public class ExceptionManager {
		
	/**
	 * Just print information about the exception
	 * 
	 * @param Exception
	 * @param Generator package of the exception
	 * @param Name of the generator class
	 * @param Generation function
	 */
	public static void catchException(Exception e, String _package, String _className, String _function)
	{
		System.err.println(System.currentTimeMillis()+" controled exception in: "+_package+"."+_className+"."+_function+"() : ");
		ExceptionManager.catchException(e);
	}
	
	/**
	 * @param Exception
	 */
	public static void catchException(Exception e)
	{
		e.printStackTrace();
	}
}
