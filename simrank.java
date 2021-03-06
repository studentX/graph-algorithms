package edu.usc.goffish.gopher.sample;

import it.unimi.dsi.fastutil.longs.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import edu.usc.goffish.gofs.*;
import edu.usc.goffish.gopher.api.*;

public class simrank extends GopherSubGraph {
	
	private static final int MAX_STEPS = 30;
	
	private Long2DoubleMap _localPageRanks;
	private Long2DoubleMap _sums;
	private Long2DoubleMap _blockranksums;

	private double L1norm;
	
	private int _numSubgraphs;
	private long _numVertices;
	
	private int initflag;
	private Map<Integer, List<Integer>> adj;

	private void initialize() {

		//obtain adjacency matrix of the graph - is this the best way to do it
		
		adj = new HashMap<Integer, LinkedList<Integer>>();

		for (ITemplateVertex vertex : subgraph.vertices()) {
			adj.put(vertex.getId(), new LinkedList<Long>());
			for (ITemplateEdge edge : vertex.outEdges()) {	
				ITemplateVertex sink = edge.getSink(vertex);
				adj.get(vertex.getId()).add(sink.getId());
			}
		}

		SubGraphMessage s1 = new SubGraphMessage(Message.adjlistMessage(adj).toBytes());	// toAdd: message handling method 
		for (int partitionId : partitions) {
			sendMessage(partitionId, s1);
		}

	}
	
	@Override
	public void compute(List<SubGraphMessage> stuff) {	
		if (superStep == 0) {
			initialize();
		}
		else if (superStep == 1) {
			//receive messages
			//decode

			//init sim matrix - identity matrix
		}
		else{
			// receive messages
			// update sim matrix from values in the messages
			// compute sim values
		}

		// send out updated sim values or matrix	
	}

	

	// taken from pagerank
	private List<Message> decode(List<SubGraphMessage> stuff) {
		if (stuff.isEmpty()) {
			return Collections.emptyList();
		}

		System.out.println("stuffsize at superstep "+superStep+" on the subgraph " + subgraph.getId() + " = "+ stuff.size());
		int messcount=0;
		ArrayList<Message> messages = new ArrayList<>(stuff.size());
		for (SubGraphMessage s : stuff) {
	
			messcount++;
			Message m = Message.fromBytes(s.getData());
			if (m.SubgraphMessage) {
				_numSubgraphs++;
			} else if (m.NumVertices > 0) {
				_numVertices += m.NumVertices;
			} else {
				messages.add(m);
			}
		}

		System.out.println("messcount at superstep "+superStep+" on the subgraph " + subgraph.getId() + " = "+ messcount);

		return messages;
	}
	
	static class Message {
		
		final boolean SubgraphMessage;
		final int NumVertices;
		final long LocalVertexId;
		
		private Message(boolean subgraphMessage) {
			SubgraphMessage = subgraphMessage;
			LocalVertexId = Long.MIN_VALUE;
			NumVertices = 0;
		}
		
		static Message subgraphMessage() {
			return new Message(true, false, false);
		}
}
		
		private Message(int numVertices) {
			LocalVertexId = Long.MIN_VALUE;
			NumVertices = numVertices;
			SubgraphMessage = false;
		}
		
		static Message numVerticesMessage(int numVertices) {
			return new Message(numVertices);
		}
		
		Message() {
			LocalVertexId = Long.MIN_VALUE;
			NumVertices = 0;
			SubgraphMessage = false;
		}
		
		Message(long localVertexId) {
			LocalVertexId = localVertexId;
			NumVertices = 0;
			SubgraphMessage = false;
		}
		
		private Message(boolean subgraphMessage, int numVertices, long localVertexId) {
			SubgraphMessage = subgraphMessage;
			NumVertices = numVertices;
			LocalVertexId = localVertexId;
		}
		
		byte[] toBytes() {
			return (Boolean.toString(SubgraphMessage) + "," + Integer.toString(NumVertices) + "," + Long.toString(LocalVertexId);
		}
		
		static Message fromBytes(byte[] bytes) {
			String[] s = new String(bytes).split(",");
			return new Message(Boolean.parseBoolean(s[0]), Boolean.parseBoolean(s[1]), Boolean.parseBoolean(s[2]));
		}
	}
}
