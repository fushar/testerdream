package fushar;

import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.Language;
import com.topcoder.shared.problem.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The main class of the plugin.
 *
 * @author fushar
 * @version 1.3.1
 */
public class TesterDream
{
	private HashMap
	<String, String> 	m_Tags;
	private Language 	m_Language;
	private DataType 	m_ReturnType;
	private DataType[] 	m_ParamTypes;
	private TestCase[]  m_TestCases;
	private String		m_ProblemName;
	private String		m_MethodName;
	private String[]	m_ParamNames;
	private double 		m_Points;
	private long		m_Time;

	/**
	 * Called once when a problem is opened, and returns the modified source code.
	 *
	 * @param source The existing source which will be blank (if first opened), writer supplied source code or source code from the last save/test/compile.
	 * @param problem The problem description.
	 * @param lang The selected language.
	 * @param render The problem renderer.
	 * @return A string representing the source after modification.
	 */
	public String preProcess(String source, ProblemComponentModel problem, Language lang, Renderer render)
	{
		m_ReturnType = problem.getReturnType();
		m_ParamTypes = problem.getParamTypes();
		m_TestCases  = problem.getTestCases();
		m_Points     = problem.getPoints();
		m_ProblemName= problem.getClassName();
		m_MethodName = problem.getMethodName();
		m_ParamNames = problem.getParamNames();
		m_Time		 = System.currentTimeMillis()/1000;
		m_Language 	 = lang;

		m_Tags = new HashMap<String, String>();

		StringBuffer code = new StringBuffer();

		generateMainCode(code);

		m_Tags.put("$TESTCODE$", code.toString());
		return "";
	}

	/**
	 * Returns a modified source code before it is returned to the applet.
	 *
	 * @param source The modified source code from preProcess() method.
	 * @param lang The selected language
	 * @return The final source code to be returned to the applet.
	 */
	public String postProcess(String source, Language lang)
	{
		StringBuffer newSource = new StringBuffer(source);
		newSource.append("\n// Powered by TesterDream 1.3.1 by fushar (August 9 2015)");
		return newSource.toString();
	}

	/**
	 * Returns a Map that has the tag to replace as the key and the source code to replace that tag as the value.
	 * @return The defined tags map.
	 */
	public Map<String, String> getUserDefinedTags()
    {
        return m_Tags;
    }

	/**
	 * Gets the base name of a type.
	 * @param type The type.
	 */
	private String getBaseName(DataType type)
	{
		if (type.getBaseName().toLowerCase().equals("long"))
			return "long long";
		return type.getBaseName().toLowerCase();
	}

	/**
	 * Generates and appends the main() code.
	 * @param code The current source code.
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
		code.append("\t\tcout << \"Testing "+m_ProblemName+" ("+m_Points+" points)\" << endl << endl;\n");
		code.append("\t\tfor (int i = 0; i < 20; i++)\n");
		code.append("\t\t{\n");
		code.append("\t\t\tostringstream s; s << argv[0] << \" \" << i;\n");
		code.append("\t\t\tint exitCode = system(s.str().c_str());\n");
		code.append("\t\t\tif (exitCode)\n");
		code.append("\t\t\t\tcout << \"#\" << i << \": Runtime Error\" << endl;\n");
		code.append("\t\t}\n");
		code.append("\t\tint T = time(NULL)-"+m_Time+";\n");
		code.append("\t\tdouble PT = T/60.0, TT = 75.0;\n");
		code.append("\t\tcout.setf(ios::fixed,ios::floatfield);\n");
		code.append("\t\tcout.precision(2);\n");
		code.append("\t\tcout << endl;\n");
		code.append("\t\tcout << \"Time  : \" << T/60 << \" minutes \" << T%60 << \" secs\" << endl;\n");
		code.append("\t\tcout << \"Score : \" << "+m_Points+"*(.3+(.7*TT*TT)/(10.0*PT*PT+TT*TT)) << \" points\" << endl;\n");
		code.append("\t}\n");
		code.append("\telse\n");
		code.append("\t{\n");
		code.append("\t\tint _tc; istringstream(argv[1]) >> _tc;\n");
		generateTestCode(code);
		generateVerifyCode(code);
		code.append("\t}\n");
		code.append("}\n");
	}

	/**
	 * Generates and appends the output verification code.
	 * @param code The current source code.
	 */
	private void generateVerifyCode(StringBuffer code)
	{
		code.append("\t\tcout.setf(ios::fixed,ios::floatfield);\n");
		code.append("\t\tcout.precision(2);\n");
		code.append("\t\tdouble _elapsed = (double)(clock()-_start)/CLOCKS_PER_SEC;\n");
		// If return type is double or vector<double>
		if (m_ReturnType.getDescriptor(m_Language).equals("double"))
			code.append("\t\tif (abs(_expected-_received) < 1e-9 || (_received > min(_expected*(1.0-1e-9), _expected*(1.0+1e-9)) && _received < max(_expected*(1.0-1e-9), _expected*(1.0+1e-9))))\n");
		else if (m_ReturnType.getDescriptor(m_Language).equals("vector <double>"))
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
		if (m_ReturnType.getBaseName().toLowerCase().equals("double"))
			code.append("\t\t\tcout.precision(10);\n");

		if (m_ReturnType.getDimension() == 0)
		{
			// If return type is string, surround output with "
			if (m_ReturnType.getDescriptor(m_Language).equals("string"))
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
			if (m_ReturnType.getBaseName().toLowerCase().equals("string"))
				code.append("\t\t\t\tcout << \" \\\"\" << _expected[i] << \"\\\"\";\n");
			else
				code.append("\t\t\t\tcout << \" \" << _expected[i];\n");
			code.append("\t\t\t}\n");
			code.append("\t\t\tcout << \" }\" << endl;\n");

			code.append("\t\t\tcout << \"           Received: {\";\n");
			code.append("\t\t\tfor (unsigned i = 0; i < _received.size(); i++)\n");
			code.append("\t\t\t{\n");
			code.append("\t\t\t\tif (i) cout << \",\";\n");
			if (m_ReturnType.getBaseName().toLowerCase().equals("string"))
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
	 * @param code The current source code.
	 */
	private void generateTestCode(StringBuffer code)
	{
		code.append("\t\t"+m_ProblemName+" _obj;\n");
		code.append("\t\t"+m_ReturnType.getDescriptor(m_Language) + " _expected, _received;\n");
		code.append("\t\ttime_t _start = clock();\n");
		code.append("\t\tswitch (_tc)\n\t\t{\n");
		for (int i = 0; i < m_TestCases.length; i++)
		{
			String[] input = m_TestCases[i].getInput();
			String output = m_TestCases[i].getOutput();
			code.append("\t\t\tcase "+i+":\n");
			code.append("\t\t\t{\n");
			for (int j = 0; j < m_ParamTypes.length; j++)
				generateParameterCode(code, true, m_ParamTypes[j], m_ParamNames[j], input[j]);

			if (m_ReturnType.getDimension() == 0)
				generateParameterCode(code, false, m_ReturnType, "_expected", output);
			else
			{
				generateParameterCode(code, true, m_ReturnType, "__expected", output);
				code.append("\t\t\t\t_expected = "+m_ReturnType.getDescriptor(m_Language)+"(__expected, __expected+sizeof(__expected)/sizeof(" + getBaseName(m_ReturnType) + "));\n");
			}
			code.append("\t\t\t\t_received = _obj."+m_MethodName+"(");
			for (int j = 0; j < m_ParamNames.length; j++)
			{
				if (j > 0) code.append(", ");
				if (m_ParamTypes[j].getDimension() == 0)
					code.append(m_ParamNames[j]);
				else
					code.append(m_ParamTypes[j].getDescriptor(m_Language)+"("+m_ParamNames[j]+", "+m_ParamNames[j]+"+sizeof("+m_ParamNames[j]+")/sizeof(" + getBaseName(m_ParamTypes[j]) + "))");
			}
			code.append("); break;\n");
			code.append("\t\t\t}\n");
		}

		// Custom test cases
		for (int i = 0; i < 3; i++)
		{
			code.append("\t\t\t/*case "+(m_TestCases.length+i)+":\n");
			code.append("\t\t\t{\n");
			for (int j = 0; j < m_ParamTypes.length; j++)
				generateParameterCode(code, true, m_ParamTypes[j], m_ParamNames[j], "");

			if (m_ReturnType.getDimension() == 0)
				generateParameterCode(code, false, m_ReturnType, "_expected", "");
			else
			{
				generateParameterCode(code, true, m_ReturnType, "__expected", "");
				code.append("\t\t\t\t_expected = "+m_ReturnType.getDescriptor(m_Language)+"(__expected, __expected+sizeof(__expected)/sizeof("+getBaseName(m_ReturnType)+"));\n");
			}
			code.append("\t\t\t\t_received = _obj."+m_MethodName+"(");
			for (int j = 0; j < m_ParamNames.length; j++)
			{
				if (j > 0) code.append(", ");
				if (m_ParamTypes[j].getDimension() == 0)
					code.append(m_ParamNames[j]);
				else
					code.append(m_ParamTypes[j].getDescriptor(m_Language)+"("+m_ParamNames[j]+", "+m_ParamNames[j]+"+sizeof("+m_ParamNames[j]+")/sizeof("+getBaseName(m_ParamTypes[j])+"))");
			}
			code.append("); break;\n");
			code.append("\t\t\t}*/\n");
		}
		code.append("\t\t\tdefault: return 0;\n");
		code.append("\t\t}\n");
	}

	/**
	 * Generates and appends the parameter code.
	 * @param code The current source code.
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
			String sfront = "";
			if (front)
				sfront = type.getDescriptor(m_Language)+ " ";

			if (type.getBaseName().toLowerCase().equals("long"))
				code.append(sfront + name + " = " + value + "LL;\n");
			else
				code.append(sfront + name + " = " + value + ";\n");
		}
		else
		{
			String typeName;
			if (type.getBaseName().toLowerCase().equals("long"))
				typeName = "long long " + name + "[] = ";
			else
				typeName = type.getBaseName().toLowerCase()+ " " + name + "[] = ";
			String[] values = value.split("\n");

			code.append(typeName+values[0]);
			for (int i = 1; i < values.length; i++)
			{
				code.append("\n\t\t\t\t");
				for (int j = 0; j < typeName.length(); j++)
					code.append(" ");
				code.append(values[i]);
				if (type.getBaseName().toLowerCase().equals("long"))
					code.append("LL");
			}
			code.append(";\n");
		}
	}
}
