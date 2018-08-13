package uk.gov.dwp.carersallowance.utils;

/**
 * validate mandatory parameters
 *
 * @author David Hutchinson (drh@elegantsoftwaresolutions.co.uk)
 * on 4 Jun 2005.
 */
public class Parameters {
    private Parameters() {
        // all methods will be static
    }

    /**
     * Validate that all the parameters supplied in args are non-null
     * If any are null throw an IllegalArgumentException indicating the first non-null
     * argument, using the name (same index from names) in exception text.
     *
     * If args == null DO NOT throw an exception
     * @param args the arguments to check
     * @param names the names of the arguments to be used when exceptions are thrown
     * @throws ValidateArgsException if args and names are of different sizes
     */
    public static void validateMandatoryArgs(Object[] args, String[] names) {
        if(args == null) {
            return;
        }
        
        if(names == null || names.length != args.length) {
            throw new ValidateArgsException("names parameter and args parameter do not match");
        }
        
        for(int index = 0; index < args.length; index++) {
            if(args[index] == null) {
                throw new IllegalArgumentException(names[index] + " cannot be null.");
            }
        }
    }

    /**
     * Validate that the parameter supplied in arg is non-null
     * If it is null throw an IllegalArgumentException indicating the
     * argument, using the supplied name in the exception text.
     *
     * @param <T> the arg type
     * @param arg the argument to check
     * @param name the name of the argument to be used when an exception is thrown
     * @return the supplied arg (or throw an exception)
     */
    public static <T extends Object> T validateMandatoryArgs(T arg, String name) {
        if(arg == null) {
            throw new IllegalArgumentException(name + " cannot be null.");
        }
        
        return arg;
    }

    /**
     * Validate that all the arguments supplied in args are null or the of the matching
     * type in types.
     * If any are not throw an IllegalArgumentException indicating the first incorrect
     * argument, using the name (same index from names) in exception text.
     *
     * If args == null DO NOT throw an exception
     * @param args the arguments to check
     * @param types the object types
     * @param names the names of the arguments to be used when exceptions are thrown
     * @throws ValidateArgsException if args and names are of different sizes
     */
    public static void validateArgTypes(Object[] args, Class<?>[] types) {
        if(args == null) {
            return;
        }

        if(types == null || types.length != args.length) {
            throw new ValidateArgsException("types parameter and args parameter do not match");
        }

        for(int index = 0; index < args.length; index++) {
            if(args[index] != null && types[index].isInstance(args[index]) == false) {
                Class<?> argClass = args[index].getClass();
                throw new IllegalArgumentException("parameter " + index + " type: " + argClass + " is not of the expected type: "+ types[index]);
            }
        }
    }

    /**
     * An IllegalArgumentException indicating that the arguments to these functions are illegal.
     * Used in order to distinguish it from an ordinary IllegalArgumentException thrown in the
     * normal behaviour of the functions.
     */
    public static class ValidateArgsException extends IllegalArgumentException {
        private static final long serialVersionUID = 7380826752015136721L;
        
        public ValidateArgsException() {
            super();
        }
        
        public ValidateArgsException(String message) {
            super(message);
        }
    }
}
