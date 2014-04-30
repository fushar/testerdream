package fushar;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.*;

import java.util.Map;
import java.util.HashMap;

/**
 * The main class of the plugin
 * 
 * @author Ashar Fuadi <fushar@gmail.com>
 * @version 1.2.4
 */
public class TesterDream
{
	private Map<String, String> tags;
	private Language languages;
	private DataType returnType;
	private DataType[] paramTypes;
	private TestCase[] testCases;
	private String problemName;
	private String methodName;
	private String[] paramNames;
	private double points;
	private long time;
	
	/**
	 * Modifies the source code after the problem is opened
	 * 
	 * This function will be called exactly after the problem is opened.
	 * 
	 * @param source The initial source code.
	 * @param problem The problem description.
	 * @param lang The selected language.
	 * @param render The problem renderer.
	 * @return The source code after modification.
	 */
	public String preProcess(String source, ProblemComponentModel problem, Language lang, Renderer render)
	{
		returnType = problem.getReturnType();
		paramTypes = problem.getParamTypes();
		testCases = problem.getTestCases();
		points = problem.getPoints();
		problemName = problem.getClassName();
		methodName = problem.getMethodName();
		paramNames = problem.getParamNames();
		time = System.currentTimeMillis() / 1000;
		languages = lang;
		
		StringBuffer code = new StringBuffer();
    if (lang.getName() == "C++") {
      generateMainCode(code);
    } else {
      generatePyMainCode(code);
    }
		
		tags = new HashMap<String, String>();
		tags.put("$TESTCODE$", code.toString());

		return "";
	}
	
	/**
	 * Modifies the source code before it is returned to the applet.
	 * 
	 * @param source The current source code.
	 * @param lang The selected language.
	 * @return The final source code to be returned to the applet.
	 */
	public String postProcess(String source, Language lang)
	{
		StringBuffer newSource = new StringBuffer(source);
    if (lang.getName() == "C++") {
      newSource.append("\n// Powered by TesterDream 1.2.4 by fushar (December 19 2012)");
    } else {
      newSource.append("\n// Powered by TesterDream 1.2.4 by fushar. Pythonized by dolphinigle.\n");
    }
		return newSource.toString();
	}
	
	/**
	 * Returns a Map representing the tags used in source code.
	 *
	 * The tags will be used to replace all keys (such as $TESTCODE$) with the corresponding values.
	 * 
	 * @return The tags.
	 */
	public Map<String, String> getUserDefinedTags()
    {
        return tags;
    }
	
	/**
	 * Returns the base name of a type.
	 * 
	 * @param type The type.
	 * @return String The base name of the type.
	 */
	private String getBaseName(DataType type)
	{
		// long long type is a special case
		if (type.getBaseName().toLowerCase().equals("long"))
			return "long long";

		return type.getBaseName().toLowerCase();
	}
	
	/**
	 * Generates and appends the main code.
	 * 
	 * @param code The currently generated source code.
	 */
	private void generateMainCode(StringBuffer code)
	{
		code.append("#include <ctime>\n");
		code.append("#include <cmath>\n");
		code.append("#include <string>\n");
		code.append("#include <vector>\n");
		code.append("#include <sstream>\n");
		code.append("#include <iostream>\n");
		code.append("#include <algorithm>\n");
		code.append("using namespace std;\n\n");
		code.append("int main(int argc, char* argv[])\n{\n");
		code.append("\tif (argc == 1) \n");
		code.append("\t{\n");
		code.append("\t\tcout << \"Testing " + problemName + " (" + points + " points)\" << endl << endl;\n");
		code.append("\t\tfor (int i = 0; i < 20; i++)\n");
		code.append("\t\t{\n");
		code.append("\t\t\tostringstream s; s << argv[0] << \" \" << i;\n");
		code.append("\t\t\tint exitCode = system(s.str().c_str());\n");
		code.append("\t\t\tif (exitCode)\n");
		code.append("\t\t\t\tcout << \"#\" << i << \": Runtime Error\" << endl;\n");
		code.append("\t\t}\n");
		code.append("\t\tint T = time(NULL)-" + time + ";\n");
		code.append("\t\tdouble PT = T/60.0, TT = 75.0;\n");
		code.append("\t\tcout.setf(ios::fixed,ios::floatfield);\n");
		code.append("\t\tcout.precision(2);\n");
		code.append("\t\tcout << endl;\n");
		code.append("\t\tcout << \"Time  : \" << T/60 << \" minutes \" << T%60 << \" secs\" << endl;\n");
		code.append("\t\tcout << \"Score : \" << " + points + "*(.3+(.7*TT*TT)/(10.0*PT*PT+TT*TT)) << \" points\" << endl;\n");
		code.append("\t}\n");
		code.append("\telse\n");
		code.append("\t{\n");
		code.append("\t\tint _tc; istringstream(argv[1]) >> _tc;\n");
		generateTestCode(code);
		generateVerifyCode(code);
		code.append("\t}\n");
		code.append("}\n");
	}
	
	private void generatePyMainCode(StringBuffer code)
	{
    code.append("'''\n");
		code.append("if __name__ == '__main__':\n");
		code.append("  import sys\n");
		code.append("  import subprocess\n");
		code.append("  if len(sys.argv) == 1:\n");
		code.append("    print 'Testing " + problemName + "(" + points + " points)'\n");
		code.append("    for i in xrange(20):\n");
    code.append("      if subprocess.call(['python'] + sys.argv + [str(i)]):\n");
    code.append("        print '#{0}: Runtime Error'.format(i)\n");
		code.append("  else:\n");
		code.append("    _tc = int(sys.argv[1])\n");
		generatePyTestCode(code);
		generatePyVerifyCode(code);
    code.append("'''\n");
	}


	/**
	 * Generates and appends the output verification code.
	 * 
	 * @param code The currently generated source code.
	 */
	private void generatePyVerifyCode(StringBuffer code)
	{
		// If return type is double or vector<double>
    code.append("    def _CheckDouble(d1, d2):\n");
    code.append("      return abs(d1 - d2) < 1e-9 or (d2 > min([d1 * (1.0 - 1e-9), d1 * (1.0 + 1e-9)]) and d2 < max([d1 * (1.0 - 1e-9), d1 * (1.0 + 1e-9)]))\n");
		if (returnType.getBaseName().equals("double") && returnType.getDimension() == 0) {
      code.append("    if _CheckDouble(_expected, _receive):\n");
    } else if (returnType.getBaseName().equals("double") && returnType.getDimension() == 1) {
      code.append("    _passed = False\n");
      code.append("    for _ex, _re in zip(_expected, _received):\n");
      code.append("      _passed = _passed and _CheckDouble(_ex, _re)\n");
			code.append("    if _passed:\n");
		}
		else {
			code.append("    if _received == _expected:\n");
    }
    code.append("      print '#{0} Passed'.format(_tc)\n");
		code.append("    else:\n");
    code.append("      print '#{0} Failed :-('.format(_tc)\n");
		
    code.append("      print 'Expected: {0}'.format(_expected)\n");
    code.append("      print 'Received: {0}'.format(_received)\n");
	}
	

	/**
	 * Generates and appends the output verification code.
	 * 
	 * @param code The currently generated source code.
	 */
	private void generateVerifyCode(StringBuffer code)
	{
		code.append("\t\tcout.setf(ios::fixed,ios::floatfield);\n");
		code.append("\t\tcout.precision(2);\n");
		code.append("\t\tdouble _elapsed = (double)(clock()-_start)/CLOCKS_PER_SEC;\n");

		// If return type is double or vector<double>
		if (returnType.getDescriptor(languages).equals("double"))
			code.append("\t\tif (abs(_expected-_received) < 1e-9 || (_received > min(_expected*(1.0-1e-9), _expected*(1.0+1e-9)) && _received < max(_expected*(1.0-1e-9), _expected*(1.0+1e-9))))\n");
		else if (returnType.getDescriptor(languages).equals("vector <double>"))
		{
			code.append("\t\tbool _passed = _expected.size() == _received.size();\n");
			code.append("\t\tfor (unsigned i = 0; i < _received.size(); i++)\n");
			code.append("\t\t\t_passed = _passed && (abs(_expected[i]-_received[i]) < 1e-9 || (_received[i] > min(_expected[i]*(1.0-1e-9), _expected[i]*(1.0+1e-9)) && _received[i] < max(_expected[i]*(1.0-1e-9), _expected[i]*(1.0+1e-9))));\n");
			code.append("\t\tif (_passed)\n");
		}
		else
			code.append("\t\tif (_received == _expected)\n");

		code.append("\t\t\tcout << \"#\" << _tc << \": Passed (\" << _elapsed << \" secs)\" << endl;\n");
		code.append("\t\telse\n");
		code.append("\t\t{\n");
		code.append("\t\t\tcout << \"#\" << _tc << \": Failed (\" << _elapsed << \" secs)\" << endl;\n");
		
		// Set precision to 10
		if (returnType.getBaseName().toLowerCase().equals("double"))
			code.append("\t\t\tcout.precision(10);\n");
		
		if (returnType.getDimension() == 0)
		{
			// If return type is string, surround output with "
			if (returnType.getDescriptor(languages).equals("string"))
			{
				code.append("\t\t\tcout << \"           Expected: \" << \"\\\"\" << _expected << \"\\\"\" << endl;\n");
				code.append("\t\t\tcout << \"           Received: \" << \"\\\"\" << _received << \"\\\"\" << endl;\n");
			}
			else
			{
				code.append("\t\t\tcout << \"           Expected: \" << _expected << endl;\n");
				code.append("\t\t\tcout << \"           Received: \" << _received << endl;\n");
			}
		}
		else
		{
			code.append("\t\t\tcout << \"           Expected: {\";\n");
			code.append("\t\t\tfor (unsigned i = 0; i < _expected.size(); i++)\n");
			code.append("\t\t\t{\n");
			code.append("\t\t\t\tif (i) cout << \",\";\n");

			// If return type is vector<string>, surround each output with "
			if (returnType.getBaseName().toLowerCase().equals("string"))
				code.append("\t\t\t\tcout << \" \\\"\" << _expected[i] << \"\\\"\";\n");
			else
				code.append("\t\t\t\tcout << \" \" << _expected[i];\n");

			code.append("\t\t\t}\n");
			code.append("\t\t\tcout << \" }\" << endl;\n");
			
			code.append("\t\t\tcout << \"           Received: {\";\n");
			code.append("\t\t\tfor (unsigned i = 0; i < _received.size(); i++)\n");
			code.append("\t\t\t{\n");
			code.append("\t\t\t\tif (i) cout << \",\";\n");

			// If return type is vector<string>, surround each output with "
			if (returnType.getBaseName().toLowerCase().equals("string"))
				code.append("\t\t\t\tcout << \" \\\"\" << _received[i] << \"\\\"\";\n");
			else
				code.append("\t\t\t\tcout << \" \" << _received[i];\n");

			code.append("\t\t\t}\n");
			code.append("\t\t\tcout << \" }\" << endl;\n");
		}
		code.append("\t\t}\n");
	}
	
	/**
	 * Generates and appends the test cases code.
	 * 
	 * @param code The currently generated source code.
	 */
	private void generateTestCode(StringBuffer code)
	{
		code.append("\t\t" + problemName + " _obj;\n");
		code.append("\t\t" + returnType.getDescriptor(languages) + " _expected, _received;\n");
		code.append("\t\ttime_t _start = clock();\n");
		code.append("\t\tswitch (_tc)\n\t\t{\n");
		for (int i = 0; i < testCases.length; i++)
		{
			String[] input = testCases[i].getInput();
			String output = testCases[i].getOutput();
			code.append("\t\t\tcase " + i + ":\n");
			code.append("\t\t\t{\n");
			for (int j = 0; j < paramTypes.length; j++)
				generateParameterCode(code, true, paramTypes[j], paramNames[j], input[j]);
			
			if (returnType.getDimension() == 0)
				generateParameterCode(code, false, returnType, "_expected", output);
			else
			{
				generateParameterCode(code, true, returnType, "__expected", output);
				code.append("\t\t\t\t_expected = " + returnType.getDescriptor(languages) + "(__expected, __expected+sizeof(__expected)/sizeof(" + getBaseName(returnType) + "));\n");
			}
			code.append("\t\t\t\t_received = _obj." + methodName + "(");
			for (int j = 0; j < paramNames.length; j++)
			{
				if (j > 0) code.append(", ");
				if (paramTypes[j].getDimension() == 0)
					code.append(paramNames[j]);
				else
					code.append(paramTypes[j].getDescriptor(languages) + "(" + paramNames[j] + ", " + paramNames[j] + "+sizeof(" + paramNames[j] + ")/sizeof(" + getBaseName(paramTypes[j]) + "))");
			}
			code.append("); break;\n");
			code.append("\t\t\t}\n");
		}
		
		// Custom test cases
		for (int i = 0; i < 3; i++)
		{
			code.append("\t\t\t/*case "+(testCases.length+i)+":\n");
			code.append("\t\t\t{\n");
			for (int j = 0; j < paramTypes.length; j++)
				generateParameterCode(code, true, paramTypes[j], paramNames[j], "");
			
			if (returnType.getDimension() == 0)
				generateParameterCode(code, false, returnType, "_expected", "");
			else
			{
				generateParameterCode(code, true, returnType, "__expected", "");
				code.append("\t\t\t\t_expected = " + returnType.getDescriptor(languages) + "(__expected, __expected+sizeof(__expected)/sizeof(" + getBaseName(returnType) + "));\n");
			}
			code.append("\t\t\t\t_received = _obj." + methodName + "(");
			for (int j = 0; j < paramNames.length; j++)
			{
				if (j > 0) code.append(", ");
				if (paramTypes[j].getDimension() == 0)
					code.append(paramNames[j]);
				else
					code.append(paramTypes[j].getDescriptor(languages) + "(" + paramNames[j] + ", " + paramNames[j] + "+sizeof(" + paramNames[j] + ")/sizeof(" + getBaseName(paramTypes[j]) + "))");
			}
			code.append("); break;\n");
			code.append("\t\t\t}*/\n");
		}
		code.append("\t\t\tdefault: return 0;\n");
		code.append("\t\t}\n");
	}

	private void generatePyTestCode(StringBuffer code)
	{
		code.append("    _obj = " + problemName + "()\n");
    code.append("    _expected = None\n");
    code.append("    _received = None\n");
		for (int i = 0; i < testCases.length; i++)
		{
			code.append("    if _tc == " + i + ":\n");
			String[] input = testCases[i].getInput();
			String output = testCases[i].getOutput();
			for (int j = 0; j < paramTypes.length; j++)
				generatePyParameterCode(code, true, paramTypes[j], paramNames[j], input[j]);
			
			if (returnType.getDimension() == 0)
				generatePyParameterCode(code, false, returnType, "_expected", output);
			else
			{
				generatePyParameterCode(code, true, returnType, "_expected", output);
			}
			code.append("      _received = _obj." + methodName + "(");
			for (int j = 0; j < paramNames.length; j++)
			{
        code.append(paramNames[j] + ", ");
			}
			code.append(")\n");
		}
	}
	
	/**
	 * Generates and appends the parameter code.
	 * 
	 * @param code The currently generated source code.
	 * @param front Whether the type should be present before the variable.
	 * @param type The type of the variable.
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	private void generateParameterCode(StringBuffer code, boolean front, DataType type, String name, String value)
	{
		code.append("\t\t\t\t");
		if (type.getDimension() == 0)
		{
			String sfront = front ? type.getDescriptor(languages) + " " : "";
			
			if (getBaseName(type).equals("long long"))
				code.append(sfront + name + " = " + value + "LL;\n");
			else
				code.append(sfront + name + " = " + value + ";\n");
		}
		else
		{
			String typeName = getBaseName(type) + " " + name + "[] = ";
			String[] values = value.split("\n");
			
			code.append(typeName + values[0]);
			for (int i = 1; i < values.length; i++)
			{
				code.append("\n\t\t\t\t");
				for (int j = 0; j < typeName.length(); j++)
					code.append(" ");
				code.append(values[i]);
				if (getBaseName(type).equals("long long"))
					code.append("LL");
			}
			code.append(";\n");
		}
	}


	private void generatePyParameterCode(StringBuffer code, boolean front, DataType type, String name, String value)
	{
		code.append("      ");
		if (type.getDimension() == 0)
		{
      code.append(name + " = " + value + "\n");
		}
		else
		{
      code.append(name + " = [" + value.substring(1, value.length() - 1) + "]\n");
		}
	}

}
