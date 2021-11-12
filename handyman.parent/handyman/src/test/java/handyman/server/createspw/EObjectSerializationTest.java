package handyman.server.createspw;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.junit.Test;

import com.google.inject.Injector;

import in.handyman.DslStandaloneSetup;
import in.handyman.dsl.Action;
import in.handyman.parser.antlr.DslParser;

public class EObjectSerializationTest {

	@Test
	@SuppressWarnings("deprecation")
	public void readGenerateASTAndConvertToProcess() throws IOException {
		try {
			DslStandaloneSetup sparrowHero = new DslStandaloneSetup();
			Injector guiceInjector = sparrowHero.createInjectorAndDoEMFRegistration();
			DslParser parser = guiceInjector.getInstance(DslParser.class);
			String filePath = this.getClass().getResource("ffsc.funnelstatus.process.spw").getPath();
			File file = new File(filePath);
			IParseResult result = parser.parse(new FileReader(file));
			EObject eRoot = result.getRootASTElement();
			Serializer serializer = guiceInjector.getInstance(Serializer.class);
			SaveOptions saveOptions = SaveOptions.newBuilder().noValidation().getOptions();
			String s = serializer.serialize(eRoot, saveOptions);
			System.out.println(s);
			Path path = Paths.get("/home/sanjeeya/workspace/process.spw");
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			IParseResult generatedProcessResult = parser.parse(new FileReader("/home/sanjeeya/workspace/process.spw"));
			EObject generatedProcesseRoot = generatedProcessResult.getRootASTElement();
			String generatedFile = serializer.serialize(generatedProcesseRoot, saveOptions);
			System.out.println(generatedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createEobject() {
		DslStandaloneSetup sparrowHero = new DslStandaloneSetup();
		Injector guiceInjector = sparrowHero.createInjectorAndDoEMFRegistration();
		in.handyman.dsl.impl.DslFactoryImpl dslFactory = new in.handyman.dsl.impl.DslFactoryImpl();
		in.handyman.dsl.Process processImpl = dslFactory.createProcess();
		in.handyman.dsl.Try tryImpl = dslFactory.createTry();
		in.handyman.dsl.Catch catchImpl = dslFactory.createCatch();
		in.handyman.dsl.Finally finallyImpl = dslFactory.createFinally();

		in.handyman.dsl.Expression expressionImpl1 = dslFactory.createExpression();
		expressionImpl1.setLhs("true");
		expressionImpl1.setOperator("==");
		expressionImpl1.setRhs("true");

		in.handyman.dsl.Expression expressionImpl2 = dslFactory.createExpression();
		expressionImpl2.setLhs("true");
		expressionImpl2.setOperator("==");
		expressionImpl2.setRhs("true");

		in.handyman.dsl.Expression expressionImpl3 = dslFactory.createExpression();
		expressionImpl3.setLhs("true");
		expressionImpl3.setOperator("==");
		expressionImpl3.setRhs("true");

		in.handyman.dsl.Transform transformImpl1 = dslFactory.createTransform();
		transformImpl1.setName("transform");
		transformImpl1.setOn("ffsc-sqldb");
		transformImpl1.setCondition(expressionImpl1);
		EList<String> sqlValues1 = transformImpl1.getValue();
		sqlValues1.add(
				"update spw_process_config set value='Funnel_${process-id}' where process='sample.macro' and variable='table_name';");

		in.handyman.dsl.Transform transformImpl2 = dslFactory.createTransform();
		transformImpl2.setName("transform");
		transformImpl2.setOn("ffsc-sqldb");
		transformImpl2.setCondition(expressionImpl2);
		EList<String> sqlValues2 = transformImpl2.getValue();
		sqlValues2.add(
				"update spw_process_config set value='Funnel_${process-id}' where process='sample.macro' and variable='table_name';");

		in.handyman.dsl.Transform transformImpl3 = dslFactory.createTransform();
		transformImpl3.setName("transform");
		transformImpl3.setOn("ffsc-sqldb");
		transformImpl3.setCondition(expressionImpl3);
		EList<String> sqlValues3 = transformImpl3.getValue();
		sqlValues3.add(
				"update spw_process_config set value='Funnel_${process-id}' where process='sample.macro' and variable='table_name';");

		processImpl.setName("ffsc.funnel.status");
		processImpl.setTry(tryImpl);
		processImpl.setCatch(catchImpl);
		processImpl.setFinally(finallyImpl);

		EList<Action> tryActions = tryImpl.getAction();
		tryActions.add(transformImpl1);
		EList<Action> catchActions = catchImpl.getAction();
		catchActions.add(transformImpl2);
		EList<Action> finallyActions = finallyImpl.getAction();
		finallyActions.add(transformImpl3);

		Serializer serializer = guiceInjector.getInstance(Serializer.class);
		@SuppressWarnings("deprecation")
		SaveOptions saveOptions = SaveOptions.newBuilder().noValidation().getOptions();
		String s = serializer.serialize(processImpl, saveOptions);
		System.out.println(s);
	}

}
