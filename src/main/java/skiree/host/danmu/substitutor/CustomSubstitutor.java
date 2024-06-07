package skiree.host.danmu.substitutor;

import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomSubstitutor {
    private static CustomSubstitutor instance;
    private Map<String, CustomFunction> customFunctions = new HashMap<>();

    public static synchronized CustomSubstitutor getInstance() {
        if (instance == null) {
            instance = new CustomSubstitutor();
        }
        return instance;
    }

    public CustomSubstitutor() {
        ServiceLoader<CustomFunction> load = ServiceLoader.load(CustomFunction.class);
        Iterator<CustomFunction> iterator = load.iterator();
        while (iterator.hasNext()) {
            register(iterator.next());
        }
    }

    public void register(CustomFunction function) {
        customFunctions.put(function.functionName(), function);
    }


    public String replace(String input, Map<String, String> params) {
        if (input == null) {
            return "";
        }
        String intermediateOutput = new StringSubstitutor(params).replace(input);
        Matcher matcher = Pattern.compile("\\$\\@\\{([a-zA-Z]+)\\(([^\\)]*)\\)\\}").matcher(intermediateOutput);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String functionArgsStr = matcher.group(2);
            CustomFunction function = customFunctions.get(functionName);
            if (function == null) {
                continue;
            }
            String[] functionArgs = functionArgsStr.split(",");
            try {
                Class<?>[] argTypes = new Class<?>[functionArgs.length];
                for (int i = 0; i < functionArgs.length; i++) {
                    argTypes[i] = String.class;
                }
                //根据方法名去实现类中查找方法
                Method method = function.getClass().getMethod(functionName, argTypes);
                Object result = method.invoke(function, (Object[]) functionArgs);
                matcher.appendReplacement(output, result.toString());
            } catch (Exception ex) {
                throw new RuntimeException("Error calling function: " + functionName, ex);
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }

}