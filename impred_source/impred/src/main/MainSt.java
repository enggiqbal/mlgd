//--inputgraph=/Users/iqbal/MLGD/mlgd/pipeline/impred_output/T8.dot --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=10 --lbloverlapp=1 --lblPram=5 --outputfile=/Users/iqbal/Desktop/T8_iq_output_f.dot
//--inputgraph=/Users/iqbal/Desktop/iqbalex2.dot --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=20 --outputfile=/Users/iqbal/Desktop/iqbalex2_output.dot


//--inputgraph=/Users/iqbal/MLGD/mlgd/pipeline/impred_output/T8.dot --edgeattraction=10 --nodenoderepulsion=10 --edgenoderepulsion=5 --iterations=10 --lbloverlapp=0 --lblPram=5 --outputfile=/Users/iqbal/Desktop/T8_iq_output_bignode.dot


package main;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularConstraint.SurroundingEdges;
import ocotillo.graph.layout.fdl.modular.ModularFdl;
import ocotillo.graph.layout.fdl.modular.ModularForce;
import ocotillo.serialization.dot.DotReader;
import ocotillo.serialization.dot.DotReader.DotReaderBuilder;
import ocotillo.serialization.dot.DotValueConverter.PositionConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeDimensionConverter;
import ocotillo.serialization.dot.DotValueConverter.IntegerConverter;
import ocotillo.serialization.dot.DotWriter;
import ocotillo.serialization.dot.DotWriter.DotWriterBuilder;
 

public class MainSt {
	
	
	
	//Default values
	static String inputFileName = null;
    static double eParam = 10.0;
	static double nnParam = 10.0;
	static double enParam = 5;
	static int iterations = 300;
	static String outputfile = null;
    static int lbloverlapp=0;
    static int lblPram=5;
	public static void main(String[] args) {
		
		
 
		
		
		
		
		for (int i = 0; i < args.length; i++) {
			parseOption(args[i]);
		}
		System.out.println("Running ImPred on " + inputFileName +
				" edgeAttraction: " + eParam +
				" nodeNodeRepulsion: " + nnParam +
				" edgeNodeRepulsion: " + enParam +
				" iterations: " + iterations +
				" outputfile: " + outputfile);
		
		DotReaderBuilder readerBuilder = new DotReaderBuilder();
        readerBuilder.nodeAttributes
                .convert("pos", StdAttribute.nodePosition, new PositionConverter(1))
                .convert("width,height", StdAttribute.nodeSize, new SizeConverter())
//        		.convert("label", StdAttribute.label, new IntegerConverter())
                .convert("level", StdAttribute.nodeLevel, new IntegerConverter());

        DotReader dotReader = readerBuilder.build();
        
        Graph graph =  null;
        try {
        graph =  dotReader.parseFile(new File(inputFileName));
        }catch(Exception e) {
        	System.out.print("input error: " + inputFileName);
        	e.printStackTrace();
        	System.exit(0);
        }
        
        
        
		NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<>((Collection<Edge>) new HashSet<Edge>());
		HashSet<Edge>  allEdges= new HashSet<Edge>(graph.edges());
		surroundingEdges.setDefault(allEdges);
		
		for(Node n : graph.nodes()) {
			allEdges= new HashSet<Edge>(graph.edges());
			surroundingEdges.set(n, allEdges);
		}
		ModularFdl algorithm;

		if (  lbloverlapp==1)
		 
		  algorithm  = new ModularFdl.ModularFdlBuilder(graph)
				.withForce(new ModularForce.LabelLabelRepulsion2D(lblPram) )
				.withConstraint(new SurroundingEdges(surroundingEdges))
				.build();
		else
		  algorithm  = new ModularFdl.ModularFdlBuilder(graph)
					.withForce(new ModularForce.EdgeAttraction2D(eParam))
					.withForce(new ModularForce.NodeNodeRepulsion2D(nnParam))
					.withForce(new ModularForce.EdgeNodeRepulsion2D(enParam))
					.withConstraint(new SurroundingEdges(surroundingEdges))
					.build();
			

		
		algorithm.iterate(iterations);
		
		DotWriterBuilder writerBuilder = new DotWriterBuilder();
        writerBuilder.nodeAttributes
                .convert(StdAttribute.nodePosition, "pos", new PositionConverter(1))
                .convert(StdAttribute.nodeLevel, "level", new IntegerConverter())
                .convert(StdAttribute.nodeSize, "width", new SizeDimensionConverter(0))
////              .convert(StdAttribute.label, "label", new IntegerConverter())
                .convert(StdAttribute.nodeSize, "height", new SizeDimensionConverter(1));

        DotWriter dotWriter = writerBuilder.build();
        		
        String outputFileName = outputfile;

        dotWriter.writeGraph(graph, new File(outputFileName));
        
        System.out.println("end");

	}

	 
	
	/**
	 * Parsing input arguments.
	 * 
	 * @param arg input arguments over terminal
	 */
	public static void parseOption(String arg) {
		String[] sep = arg.split("=",2);
		if ("--inputgraph".equals(sep[0])) inputFileName = sep[1];
		else if ("--edgeattraction".equals(sep[0])) eParam = Double.parseDouble(sep[1]);
		else if ("--nodenoderepulsion".equals(sep[0])) nnParam  = Double.parseDouble(sep[1]);
		else if ("--edgenoderepulsion".equals(sep[0])) enParam = Double.parseDouble(sep[1]);
		else if ("--iterations".equals(sep[0])) iterations = Integer.parseInt(sep[1]);
		else if ("--outputfile".equals(sep[0])) outputfile = sep[1];
		else if ("--lbloverlapp".equals(sep[0])) lbloverlapp=Integer.parseInt(sep[1]);
		else if ("--lblPram".equals(sep[0])) lblPram=Integer.parseInt(sep[1]);
		
		else {
			System.err.println("==> Unrecognized Option: " + arg );
			System.exit(1);
		}
	}
	
	

}
