package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	Map<Integer,Airport> aIdMap; //mappa utilizzata nel dao perchè i vertici del grafo devono essere degli oggetti Airport
	Map<Airport,Airport> visita;
	ExtFlightDelaysDAO dao;
	
	public Model() {
		grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		aIdMap= new HashMap<Integer, Airport>();
		visita= new HashMap<Airport, Airport>();
		dao= new ExtFlightDelaysDAO();
		dao.loadAllAirports(aIdMap); //popolo la mappa
		
	}
	
	public Collection<Airport> getAirports(){
		return this.aIdMap.values();
	}
	
	public void creaGrafo(int distanzaMedia) {
		
		//Aggiungere i vertici
		Graphs.addAllVertices(grafo, aIdMap.values());
		
		//Aggiungere gli archi
		for(Rotta rotta: dao.getRotte(aIdMap, distanzaMedia)) {
			//controllo se esiste già un arco
			//se esiste aggiorno il peso
			DefaultWeightedEdge edge = grafo.getEdge(rotta.getPartenza(), rotta.getDestinazione());
			if(edge==null) { //l'arco non c'è quindi lo creo
				Graphs.addEdge(grafo, rotta.getPartenza(), rotta.getDestinazione(), rotta.getDistanzaMedia());
				
			}else { //l'arco c'è già quindi aggiorno solo il peso
				System.out.println("Aggiornare peso!");
				double peso = grafo.getEdgeWeight(edge);
				double newPeso = (peso + rotta.getDistanzaMedia())/2;
				grafo.setEdgeWeight(edge, newPeso);
			}
		}
		
		System.out.println("Grafo creato!");
		System.out.println("Vertici: "+ grafo.vertexSet().size());
		System.out.println("Archi: "+ grafo.edgeSet().size());
	}
	
	public Boolean testConnessione(Integer a1, Integer a2) {
		//struttura dati in cui tener traccia degli aeroporti visitati
		Set<Airport> visitati = new HashSet<Airport>();
		
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		System.out.println("Testo connessione tra " + partenza +" e "+ destinazione);
		//visita in ampiezza per la quale definisco un iteratore
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo,partenza);
		
		//visita per mezzo dell'iteratire it
		while(it.hasNext())
			visitati.add(it.next());
		if(visitati.contains(destinazione))
			return true;
		else 
			return false;
	}
	
	public List<Airport> trovaPercorso(Integer a1, Integer a2){
		List<Airport> percorso = new ArrayList<Airport>();
		Airport partenza = aIdMap.get(a1);
		Airport destinazione = aIdMap.get(a2);
		System.out.println("Cerco percorso tra " + partenza +" e "+ destinazione);
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo,partenza);
		
		visita.put(partenza,null);
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
								
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
								
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				Airport sorgente = grafo.getEdgeSource(ev.getEdge());
				Airport destinazione = grafo.getEdgeTarget(ev.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(sorgente)) {
					visita.put(destinazione, sorgente);
				}else if(!visita.containsKey(sorgente) && visita.containsKey(destinazione)){
					visita.put(sorgente, destinazione);
				}
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
								
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
								
			}
		});
		
		while(it.hasNext())
			it.next();
		
		if(!visita.containsKey(partenza) || !visita.containsKey(destinazione))
			return null;
		
		Airport step = destinazione;
		while(!step.equals(partenza)) {
			percorso.add(step);
			step = visita.get(step);
		}
		percorso.add(step);
		return percorso;
	}
}


