package org.tartarus.snowball;
import java.lang.reflect.Method;
public class Among {
    private static final Class<?>[] EMPTY_PARAMS = new Class[0];
    public Among (String s, int substring_i, int result,
		  String methodname, SnowballProgram methodobject) {
        this.s_size = s.length();
        this.s = s.toCharArray();
        this.substring_i = substring_i;
	this.result = result;
	this.methodobject = methodobject;
	if (methodname.length() == 0) {
	    this.method = null;
	} else {
	    try {
		this.method = methodobject.getClass().
		getDeclaredMethod(methodname, EMPTY_PARAMS);
	    } catch (NoSuchMethodException e) {
		throw new RuntimeException(e);
	    }
	}
    }
    public final int s_size; 
    public final char[] s; 
    public final int substring_i; 
    public final int result;      
    public final Method method; 
    public final SnowballProgram methodobject; 
};
