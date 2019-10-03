package main;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.samples.parsers.Commons.DyDataSet;

public class Main {

	public static void main(String[] args) {

		double delta = 10.0;

		double suggestedTimeFactor = 10.0;

		
		DyGraph graph = new DyGraph();
		DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
		DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
		DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
		DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
		DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);
		
		

		Map<String, Node> nodeMap = new HashMap<>();
		for (int i=0; i<4; i++) {
			String nodeIdentifier = i + "";
			Node node = graph.newNode(nodeIdentifier);
			presence.set(node, new Evolution<>(true));
			label.set(node, new Evolution<>(nodeIdentifier));

			if(i==0)
				position.set(node, new Evolution<>(new Coordinates(0, 0)));
			else if(i==1)
				position.set(node, new Evolution<>(new Coordinates(0, 100)));
			else if(i==2)
				position.set(node, new Evolution<>(new Coordinates(200, 0)));
			else
				position.set(node, new Evolution<>(new Coordinates(100, 200)));

			color.set(node, new Evolution<>(new Color(141, 211, 199)));
			nodeMap.put(nodeIdentifier, node);
		}


		for (int i=0; i<3; i++) {
			Node source = nodeMap.get(i+"");
			Node target = nodeMap.get((i+1)+"");
			Edge edge = graph.betweenEdge(source, target);
			if (edge == null) {
				edge = graph.newEdge(source, target);
				edgePresence.set(edge, new Evolution<>(false));
				edgeColor.set(edge, new Evolution<>(Color.BLACK));
			}

			Interval participantPresence = Interval.global;
			presence.get(source).insert(new FunctionConst<>(participantPresence, true));
			presence.get(target).insert(new FunctionConst<>(participantPresence, true));
			edgePresence.get(edge).insert(new FunctionConst<>(participantPresence, true));
		}    
		
		DyNodeAttribute beforeNodePositions = graph.nodeAttribute("nodePosition");	
		for(Node n : graph.nodes()) {

			Coordinates currPosition = ((Evolution<Coordinates>)beforeNodePositions.get(n)).getDefaultValue();
			System.out.println(n.id() + " " + currPosition);

		}
		
		
		DyDataSet dataset = new DyDataSet(graph, 1, Interval.global);


		DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(graph, 0)
				.withForce(new DyModularForce.TimeStraightning(delta))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.ConnectionAttraction(delta))
				.withForce(new DyModularForce.EdgeRepulsion(delta))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
				.withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
				.withPostProcessing(new DyModularPostProcessing.FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D))
				.build();


		algorithm.iterate(100);

		
		DyNodeAttribute nodePositions = graph.nodeAttribute("nodePosition");	
		for(Node n : graph.nodes()) {

			Coordinates currPosition = ((Evolution<Coordinates>)nodePositions.get(n)).getDefaultValue();
			System.out.println(n.id() + " " + currPosition);

		}


		System.out.println("end");


	}
	
	
	

}
