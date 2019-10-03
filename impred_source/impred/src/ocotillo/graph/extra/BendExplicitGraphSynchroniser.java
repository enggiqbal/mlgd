/**
 * Copyright © 2014-2016 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.graph.extra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Element;
import ocotillo.graph.ElementAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;

/**
 * A class that builds and synchronises a bend explicit graph that mirrors a
 * graph with bent edges. Given a graph with control points on its edges
 * (bends), a bend explicit graph is a graph that replicates the original one
 * but substitutes bent edges with chains of nodes and edges.
 */
public class BendExplicitGraphSynchroniser {

    private final Graph originalGraph;
    private final Graph mirrorGraph;

    private final NodeAttribute<Coordinates> originalPositions;
    private final NodeAttribute<Coordinates> mirrorPositions;
    private final EdgeAttribute<ControlPoints> originalBends;

    private final Set<NodeAttributeToPreserve> nodeAttributesToPreserve;
    private final Set<String> edgeAttributesToPreserve;

    private final Map<Edge, MirrorEdge> directEdgeMap = new HashMap<>();
    private final Map<Element, Edge> reverseEdgeMap = new HashMap<>();

    /**
     * A builder for bend explicit graph synchronisers.
     */
    public static class BegsBuilder {

        private final Graph graph;
        private final Set<NodeAttributeToPreserve> nodeAttributesToPreserve = new HashSet<>();
        private final Set<String> edgeAttributesToPreserve = new HashSet<>();

        /**
         * Construct a builder for bend explicit graph synchronisers.
         *
         * @param graph the graph to be mirrored.
         */
        public BegsBuilder(Graph graph) {
            this.graph = graph;
        }

        /**
         * Instructs the bend explicit graph synchroniser to preserve the node
         * attribute with given ID.
         *
         * @param attributeId the attribute ID.
         * @param applySourceToBends indicates whether the value for the source
         * node should be applied to all bends.
         * @return the builder.
         */
        public BegsBuilder preserveNodeAttribute(String attributeId, boolean applySourceToBends) {
            assert (graph.hasNodeAttribute(attributeId)) : "The node attribute to preserve does not exist";
            assert (!attributeId.equals(StdAttribute.nodePosition.name())) : "The node positions is already preserved and must not be passed as attribute to preserve";
            nodeAttributesToPreserve.add(new NodeAttributeToPreserve(attributeId, applySourceToBends));
            return this;
        }

        /**
         * Instructs the bend explicit graph synchroniser to preserve the given
         * standard node attribute.
         *
         * @param attribute the standard attribute.
         * @param applySourceToBends indicates whether the value for the source
         * node should be applied to all bends.
         * @return the builder.
         */
        public BegsBuilder preserveNodeAttribute(StdAttribute attribute, boolean applySourceToBends) {
            return preserveNodeAttribute(attribute.name(), applySourceToBends);
        }

        /**
         * Instructs the bend explicit graph synchroniser to preserve the edge
         * attribute with given ID.
         *
         * @param attributeId the attribute ID.
         * @return the builder.
         */
        public BegsBuilder preserveEdgeAttribute(String attributeId) {
            assert (graph.hasEdgeAttribute(attributeId)) : "The edge attribute to preserve does not exist";
            assert (!attributeId.equals(StdAttribute.edgePoints.name())) : "The edge bends are already preserved and must not be passed as attribute to preserve";
            edgeAttributesToPreserve.add(attributeId);
            return this;
        }

        /**
         * Instructs the bend explicit graph synchroniser to preserve the given
         * standard edge attribute.
         *
         * @param attribute the standard attribute.
         * @return the builder.
         */
        public BegsBuilder preserveEdgeAttribute(StdAttribute attribute) {
            return preserveEdgeAttribute(attribute.name());
        }

        /**
         * Builds the bend explicit graph synchroniser.
         *
         * @return the bend explicit graph synchroniser.
         */
        public BendExplicitGraphSynchroniser build() {
            return new BendExplicitGraphSynchroniser(graph, nodeAttributesToPreserve, edgeAttributesToPreserve);
        }
    }

    /**
     * Returns the mirror graph.
     *
     * @return the mirror graph.
     */
    public Graph getMirrorGraph() {
        return mirrorGraph;
    }

    /**
     * Returns the position attribute for the mirror graph.
     *
     * @return the position attribute.
     */
    public NodeAttribute<Coordinates> getMirrorPositions() {
        return mirrorPositions;
    }

    /**
     * Returns the mirror edge that corresponding to an original one.
     *
     * @param edge the original edge.
     * @return the corresponding mirror edge.
     */
    public MirrorEdge getMirrorEdge(Edge edge) {
        assert (originalGraph.has(edge)) : "The original edge passed as parameter in not contained in the original graph.";
        return directEdgeMap.get(edge);
    }

    /**
     * Returns the original edge that corresponding to an element of the mirror
     * one. Returns null if the parameter element is an original node, and is
     * not therefore part or any mirror edge.
     *
     * @param mirrorElement a bend or segment of a mirror edge.
     * @return the corresponding original edge.
     */
    public Edge getOriginalEdge(Element mirrorElement) {
        assert (mirrorGraph.has(mirrorElement)) : "The mirror element passed as parameter in not contained in the mirror graph.";
        return reverseEdgeMap.containsKey(mirrorElement) ? reverseEdgeMap.get(mirrorElement) : null;
    }

    /**
     * Verifies if the mirror element passed as parameter is part of a mirror
     * edge. Mirror graph element that are not in mirror edges are original
     * nodes, which are both in the original and mirror graph.
     *
     * @param mirrorElement the mirror element.
     * @return true if the element is part of a mirror edge, false otherwise.
     */
    public boolean isPartOfMirrorEdge(Element mirrorElement) {
        return getOriginalEdge(mirrorElement) != null;
    }

    /**
     * Constructs a bend explicit graph synchroniser.
     *
     * @param graph the graph to be mirrored.
     * @param nodeAttributesToPreserve the node attribute IDs to preserve.
     * @param edgeAttributesToPreserve the edge attribute IDs to preserve.
     */
    private BendExplicitGraphSynchroniser(Graph graph, Set<NodeAttributeToPreserve> nodeAttributesToPreserve, Set<String> edgeAttributesToPreserve) {
        this.originalGraph = graph;
        this.originalPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        this.originalBends = graph.edgeAttribute(StdAttribute.edgePoints);
        this.nodeAttributesToPreserve = nodeAttributesToPreserve;
        this.edgeAttributesToPreserve = edgeAttributesToPreserve;

        mirrorGraph = new Graph();
        mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);
        createMirrorAttributes();
        updateMirror();
    }

    /**
     * Creates an attribute in the mirror graph for each of the attributes to be
     * preserved.
     */
    private void createMirrorAttributes() {
        for (NodeAttributeToPreserve attribute : nodeAttributesToPreserve) {
            assert (originalGraph.hasNodeAttribute(attribute.id)) : "The node attribute to be mirrored does not exist in the original graph.";
            mirrorGraph.newNodeAttribute(attribute.id, originalGraph.nodeAttribute(attribute.id).getDefault());
        }

        for (String attrId : edgeAttributesToPreserve) {
            assert (originalGraph.hasEdgeAttribute(attrId)) : "The edge attribute to be mirrored does not exist in the original graph.";
            mirrorGraph.newEdgeAttribute(attrId, originalGraph.edgeAttribute(attrId).getDefault());
        }
    }

    /**
     * Handles the updates from original graph to mirror. Original elements can
     * be added or removed causing the mirror graph to change accordingly.
     */
    public final void updateMirror() {
        updateOriginalNodesInMirror();
        updateOriginalEdgesInMirror();
        removeNoMoreExistingOriginalEdges();
        removeNoMoreExistingOriginalNodes();
    }

    /**
     * Updates the mirror attributes of each original node. The original node is
     * added to the mirror graph whenever the latter does not already have it as
     * an element.
     */
    private void updateOriginalNodesInMirror() {
        for (Node node : originalGraph.nodes()) {
            if (!mirrorGraph.has(node)) {
                mirrorGraph.add(node);
            }
            originalAttributesToMirror(node);
        }
    }

    /**
     * Updates the mirror chain and attributes of each original edge. A mirror
     * edge is added whenever the original edge is not already considered in the
     * mirror graph.
     */
    private void updateOriginalEdgesInMirror() {
        for (Edge edge : originalGraph.edges()) {
            if (!directEdgeMap.containsKey(edge)) {
                constructMirrorEdge(edge);
            }
            MirrorEdge mirrorEdge = directEdgeMap.get(edge);
            updateMirrorEdgeBends(edge, mirrorEdge);
            updateMirrorEdgeSegments(mirrorEdge);
            originalAttributesToMirror(edge);
        }
    }

    /**
     * Removes mirror edges that correspond to edges deleted from the original
     * graph.
     */
    private void removeNoMoreExistingOriginalEdges() {
        for (MirrorEdge mirrorEdge : directEdgeMap.values()) {
            if (!originalGraph.has(mirrorEdge.original)) {
                for (Edge segment : mirrorEdge.segments) {
                    mirrorGraph.forcedRemove(segment);
                    reverseEdgeMap.remove(segment);
                }
                for (Node bend : mirrorEdge.bends) {
                    mirrorGraph.forcedRemove(bend);
                    reverseEdgeMap.remove(bend);
                }
                directEdgeMap.remove(mirrorEdge.original);
            }
        }
    }

    /**
     * Removes mirror nodes that correspond to nodes deleted from the original
     * graph.
     */
    private void removeNoMoreExistingOriginalNodes() {
        for (Node mirrorNode : mirrorGraph.nodes()) {
            if (!isPartOfMirrorEdge(mirrorNode) && !originalGraph.has(mirrorNode)) {
                mirrorGraph.remove(mirrorNode);
            }
        }
    }

    /**
     * Construct a mirror edge and insert it in the direct map.
     *
     * @param edge the edge of the original graph.
     */
    private void constructMirrorEdge(Edge edge) {
        MirrorEdge mirrorEdge = new MirrorEdge();
        mirrorEdge.original = edge;
        mirrorEdge.source = edge.source();
        mirrorEdge.target = edge.target();
        directEdgeMap.put(edge, mirrorEdge);
    }

    /**
     * Constructs the bends of a mirror edge.
     *
     * @param edge the original edge.
     * @param mirrorEdge the mirror edge being built.
     */
    private void updateMirrorEdgeBends(Edge edge, MirrorEdge mirrorEdge) {
        while (mirrorEdge.bends.size() < originalBends.get(edge).size()) {
            Node bendNode = mirrorGraph.newNode();
            mirrorEdge.bends.add(bendNode);
            reverseEdgeMap.put(bendNode, edge);
        }
        while (mirrorEdge.bends.size() > originalBends.get(edge).size()) {
            Node bendNode = mirrorEdge.bends.removeLast();
            mirrorGraph.forcedRemove(bendNode);
            reverseEdgeMap.remove(bendNode);
        }
    }

    /**
     * Construct the segments of a mirror edge.
     *
     * @param mirrorEdge the mirror edge being built.
     */
    private void updateMirrorEdgeSegments(MirrorEdge mirrorEdge) {
        List<Node> edgeNodes = new ArrayList<>();
        edgeNodes.add(mirrorEdge.source);
        edgeNodes.addAll(mirrorEdge.bends);
        edgeNodes.add(mirrorEdge.target);

        if (numberOfBendsChanged(mirrorEdge)) {
            if (!mirrorEdge.segments.isEmpty()) {
                removeTrailingSegment(mirrorEdge);
            }
            while (mirrorEdge.segments.size() > mirrorEdge.bends.size()) {
                removeTrailingSegment(mirrorEdge);
            }
        }

        for (int i = mirrorEdge.segments.size(); i < edgeNodes.size() - 1; i++) {
            Edge segment = mirrorGraph.newEdge(edgeNodes.get(i), edgeNodes.get(i + 1));
            mirrorEdge.segments.add(segment);
            reverseEdgeMap.put(segment, mirrorEdge.original);
        }
    }

    /**
     * Verifies if the number of bends changed, requiring an update in the
     * segments previously present in the graph.
     *
     * @param mirrorEdge the mirror edge.
     * @return true if the number of segments does not correspond to the number
     * of bends, false otherwise.
     */
    private boolean numberOfBendsChanged(MirrorEdge mirrorEdge) {
        return mirrorEdge.segments.size() != mirrorEdge.bends.size() + 1;
    }

    /**
     * Removes a trailing segment from the mirror graph and related structures.
     *
     * @param mirrorEdge the mirror edge.
     */
    private void removeTrailingSegment(MirrorEdge mirrorEdge) {
        Edge trailingSegment = mirrorEdge.segments.removeLast();
        mirrorGraph.forcedRemove(trailingSegment);
        reverseEdgeMap.remove(trailingSegment);
    }

    /**
     * Copies the original attributes to the mirror graph for a given node.
     *
     * @param node the node.
     */
    private void originalAttributesToMirror(Node node) {
        for (NodeAttributeToPreserve attribute : nodeAttributesToPreserve) {
            NodeAttribute<Object> originalAttribute = originalGraph.nodeAttribute(attribute.id);
            NodeAttribute<Object> mirrorAttribute = mirrorGraph.nodeAttribute(attribute.id);
            copyAttributeValue(originalAttribute, node, mirrorAttribute, node);
        }
        copyPosition(originalPositions, mirrorPositions, node);
    }

    /**
     * Copies the original attributes to the mirror graph for a given edge.
     *
     * @param edge the edge.
     */
    private void originalAttributesToMirror(Edge edge) {
        MirrorEdge mirrorEdge = directEdgeMap.get(edge);
        for (NodeAttributeToPreserve attribute : nodeAttributesToPreserve) {
            if (attribute.applySourceToBends) {
                NodeAttribute<Object> originalAttribute = originalGraph.nodeAttribute(attribute.id);
                NodeAttribute<Object> mirrorAttribute = mirrorGraph.nodeAttribute(attribute.id);
                for (Node bend : mirrorEdge.bends) {
                    copyAttributeValue(originalAttribute, edge.source(), mirrorAttribute, bend);
                }
            }
        }
        for (String attrId : edgeAttributesToPreserve) {
            EdgeAttribute<Object> originalAttribute = originalGraph.edgeAttribute(attrId);
            EdgeAttribute<Object> mirrorAttribute = mirrorGraph.edgeAttribute(attrId);
            for (Edge segment : mirrorEdge.segments) {
                copyAttributeValue(originalAttribute, edge, mirrorAttribute, segment);
            }
        }
        copyPosition(edge, mirrorEdge);
    }

    /**
     * Handles the updates from mirror graph to original. Original elements
     * cannot be added or removed as a result of mirror graph modifications.
     * Also, mirror node attributes are copied in the original graph only for
     * original nodes. Finally, mirror edge attributes affect original edge
     * attributes only if all the segments have the same value.
     */
    public void updateOriginal() {
        for (Node node : originalGraph.nodes()) {
            mirrorAttributesToOriginal(node);
        }
        for (Edge edge : originalGraph.edges()) {
            mirrorAttributesToOriginal(directEdgeMap.get(edge));
        }
    }

    /**
     * Copies the mirror attribute of a node to the original graph.
     *
     * @param node the node.
     */
    private void mirrorAttributesToOriginal(Node node) {
        assert (!isPartOfMirrorEdge(node)) : "The method should not be called on node bends.";
        for (NodeAttributeToPreserve attribute : nodeAttributesToPreserve) {
            NodeAttribute<Object> originalAttribute = originalGraph.nodeAttribute(attribute.id);
            NodeAttribute<Object> mirrorAttribute = mirrorGraph.nodeAttribute(attribute.id);
            copyAttributeValue(mirrorAttribute, node, originalAttribute, node);
        }
        copyPosition(mirrorPositions, originalPositions, node);
    }

    /**
     * Copies the mirror attribute of a mirror edge to the corresponding
     * original one. An attribute on the mirror segments is copied to the
     * original edge only if it is the same for all segments.
     *
     * @param mirrorEdge the mirror edge.
     */
    private void mirrorAttributesToOriginal(MirrorEdge mirrorEdge) {
        Edge originalEdge = mirrorEdge.original;
        for (String attrId : edgeAttributesToPreserve) {
            EdgeAttribute<Object> originalAttribute = originalGraph.edgeAttribute(attrId);
            EdgeAttribute<Object> mirrorAttribute = mirrorGraph.edgeAttribute(attrId);
            boolean sameForAllSegments = true;
            Edge firstSegment = mirrorEdge.segments.getFirst();
            Object mirrorValue = mirrorAttribute.get(firstSegment);
            for (Edge segment : mirrorEdge.segments) {
                sameForAllSegments &= mirrorValue.equals(mirrorAttribute.get(segment));
            }
            if (sameForAllSegments) {
                copyAttributeValue(mirrorAttribute, firstSegment, originalAttribute, originalEdge);
            }
        }
        copyPosition(mirrorEdge, originalEdge);
    }

    /**
     * Adds a bend to a mirror edge by substituting a segment with a chain of
     * two new segment and a new bend. The new bend is placed on the midpoint of
     * the segment.
     *
     * @param mirrorEdge the mirror edge.
     * @param segment the segment where to add a bend.
     * @return the bend.
     */
    public Node addMirrorBend(MirrorEdge mirrorEdge, Edge segment) {
        Coordinates bendPosition = Geom.eXD.midPoint(mirrorPositions.get(segment.source()), mirrorPositions.get(segment.target()));
        return addMirrorBend(mirrorEdge, segment, bendPosition);
    }

    /**
     * Adds a bend to a mirror edge by substituting a segment with a chain of
     * two new segment and a new bend. The new bend is placed at the given
     * position.
     *
     * @param mirrorEdge the mirror edge.
     * @param segment the segment where to add a bend.
     * @param bendPosition the bend position.
     * @return the bend.
     */
    public Node addMirrorBend(MirrorEdge mirrorEdge, Edge segment, Coordinates bendPosition) {
        assert (mirrorEdge.segments.contains(segment)) : "The segment passed as parameter must belong to the mirror edge.";
        Node newBend = mirrorGraph.newNode();
        Node previousPoint = segment.source();
        Node nextPoint = segment.target();
        Edge newSegmentA = mirrorGraph.newEdge(previousPoint, newBend);
        Edge newSegmentB = mirrorGraph.newEdge(newBend, nextPoint);
        setNewBendAttributes(mirrorEdge, newBend, bendPosition, newSegmentA, newSegmentB);

        int segmIdx = mirrorEdge.segments.indexOf(segment);
        mirrorEdge.segments.remove(segmIdx);
        mirrorEdge.segments.addAll(segmIdx, Arrays.asList(newSegmentA, newSegmentB));
        mirrorEdge.bends.add(segmIdx, newBend);

        reverseEdgeMap.put(newBend, mirrorEdge.original);
        reverseEdgeMap.put(newSegmentA, mirrorEdge.original);
        reverseEdgeMap.put(newSegmentB, mirrorEdge.original);
        reverseEdgeMap.remove(segment);

        mirrorGraph.remove(segment);
        return newBend;
    }

    /**
     * Sets the attribute to preserve to the mirror edge elements created while
     * inserting a bend.
     *
     * @param mirrorEdge the mirror edge.
     * @param newBend the new bend.
     * @param bendPosition the position of the bend.
     * @param newSegmentA the incoming segment into the bend.
     * @param newSegmentB the outgoing segment from the bend.
     */
    private void setNewBendAttributes(MirrorEdge mirrorEdge, Node newBend, Coordinates bendPosition, Edge newSegmentA, Edge newSegmentB) {
        mirrorPositions.set(newBend, bendPosition);
        for (NodeAttributeToPreserve attribute : nodeAttributesToPreserve) {
            if (attribute.applySourceToBends) {
                NodeAttribute<Object> originalAttribute = originalGraph.nodeAttribute(attribute.id);
                NodeAttribute<Object> mirrorAttribute = mirrorGraph.nodeAttribute(attribute.id);
                copyAttributeValue(originalAttribute, mirrorEdge.source, mirrorAttribute, newBend);
            }
        }
        for (String attrId : edgeAttributesToPreserve) {
            EdgeAttribute<Object> originalAttribute = originalGraph.edgeAttribute(attrId);
            EdgeAttribute<Object> mirrorAttribute = mirrorGraph.edgeAttribute(attrId);
            copyAttributeValue(originalAttribute, mirrorEdge.original, mirrorAttribute, newSegmentA);
            copyAttributeValue(originalAttribute, mirrorEdge.original, mirrorAttribute, newSegmentB);
        }
    }

    /**
     * Removes the given mirror edge bend. The bend is substituted by a segment
     * connecting directly the previous and the following points in the chain.
     *
     * @param mirrorEdge the mirror edge.
     * @param bend the bend to be substituted.
     * @return the new segment.
     */
    public Edge removeMirrorBend(MirrorEdge mirrorEdge, Node bend) {
        assert (mirrorEdge.bends.contains(bend)) : "The bend passed as parameter must belong to the mirror edge.";
        Edge oldSegmentA = mirrorGraph.inEdges(bend).iterator().next();
        Edge oldSegmentB = mirrorGraph.outEdges(bend).iterator().next();
        Node previousPoint = oldSegmentA.source();
        Node nextPoint = oldSegmentB.target();
        Edge newSegment = mirrorGraph.newEdge(previousPoint, nextPoint);
        setNewSegmentAttributes(mirrorEdge, newSegment);

        int bendIdx = mirrorEdge.bends.indexOf(bend);
        mirrorEdge.bends.remove(bendIdx);
        mirrorEdge.segments.remove(bendIdx);
        mirrorEdge.segments.remove(bendIdx);
        mirrorEdge.segments.add(bendIdx, newSegment);

        reverseEdgeMap.remove(bend);
        reverseEdgeMap.remove(oldSegmentA);
        reverseEdgeMap.remove(oldSegmentB);
        reverseEdgeMap.put(newSegment, mirrorEdge.original);
        mirrorGraph.forcedRemove(bend);
        return newSegment;
    }

    /**
     * Sets the attributes of the new segment created while removing a bend.
     *
     * @param mirrorEdge the mirror edge.
     * @param segment the segment.
     */
    private void setNewSegmentAttributes(MirrorEdge mirrorEdge, Edge segment) {
        for (String attrId : edgeAttributesToPreserve) {
            EdgeAttribute<Object> originalAttribute = originalGraph.edgeAttribute(attrId);
            EdgeAttribute<Object> mirrorAttribute = mirrorGraph.edgeAttribute(attrId);
            copyAttributeValue(originalAttribute, mirrorEdge.original, mirrorAttribute, segment);
        }
    }

    /**
     * Copies an attribute value from original to mirror, or vice-versa,
     * whenever necessary. The method will not overwrite the destination value
     * if this is equal to the source one, avoiding observers to be
     * unnecessarily triggered. Also, the copy is only performed if the source
     * value is not default.
     *
     * @param <T> the type of element to be used.
     * @param sourceAttribute the source attribute.
     * @param sourceElement the source element.
     * @param destinationAttribute the destination attribute.
     * @param destinationElement the destination element.
     */
    private <T extends Element> void copyAttributeValue(ElementAttribute<T, Object> sourceAttribute, T sourceElement, ElementAttribute<T, Object> destinationAttribute, T destinationElement) {
        Object sourceValue = sourceAttribute.get(sourceElement);
        if (!sourceAttribute.isDefault(sourceElement)
                && (destinationAttribute.isDefault(destinationElement) || !sourceValue.equals(destinationAttribute.get(destinationElement)))) {
            destinationAttribute.set(destinationElement, sourceValue);
        }
    }

    /**
     * Copies the position of a node from original to mirror, or vice-versa,
     * whenever necessary. The method will not overwrite the destination value
     * if this is equal to the source one, avoiding observers to be
     * unnecessarily triggered.
     *
     * @param sourceAttribute the source attribute.
     * @param destinationAttribute the destination attribute.
     * @param node the node.
     */
    private void copyPosition(NodeAttribute<Coordinates> sourceAttribute, NodeAttribute<Coordinates> destinationAttribute, Node node) {
        Coordinates sourceValue = sourceAttribute.get(node);
        if (!sourceAttribute.isDefault(node)
                && (destinationAttribute.isDefault(node) || !sourceValue.equals(destinationAttribute.get(node)))) {
            destinationAttribute.set(node, sourceValue);
        }
    }

    /**
     * Copies the position of an edge from original to mirror, whenever
     * necessary. The method will not overwrite the node bends positions
     * whenever they are equal to the existing ones, avoiding observers to be
     * unnecessarily triggered.
     *
     * @param originalEdge the original edge.
     * @param mirrorEdge the mirror edge.
     */
    private void copyPosition(Edge originalEdge, MirrorEdge mirrorEdge) {
        ControlPoints points = originalBends.get(originalEdge);
        assert (points.size() == mirrorEdge.bends.size()) : "The number of bends in the original edge and in the mirror edge do not correspond.";
        int coordIdx = 0;
        for (Node bend : mirrorEdge.bends) {
            Coordinates position = points.get(coordIdx);
            if (mirrorPositions.isDefault(bend) || !position.equals(mirrorPositions.get(bend))) {
                mirrorPositions.set(bend, position);
            }
            coordIdx++;
        }
    }

    /**
     * Copies the bends of an edge from mirror to original, whenever necessary.
     * The method will not overwrite the edge bends positions whenever they are
     * equal to the existing ones, avoiding observers to be unnecessarily
     * triggered.
     *
     * @param mirrorEdge the mirror edge.
     * @param originalEdge the original edge.
     */
    private void copyPosition(MirrorEdge mirrorEdge, Edge originalEdge) {
        ControlPoints points = new ControlPoints();
        for (Node bend : mirrorEdge.bends) {
            points.add(mirrorPositions.get(bend));
        }
        if (!points.equals(originalBends.get(originalEdge))) {
            originalBends.set(originalEdge, points);
        }
    }

    /**
     * The information on a mirror edge.
     */
    public static class MirrorEdge {

        private Edge original;
        private Node source;
        private Node target;
        private LinkedList<Node> bends = new LinkedList<>();
        private LinkedList<Edge> segments = new LinkedList<>();

        /**
         * Returns the original edge corresponding to the mirror one.
         *
         * @return the original edge.
         */
        public Edge originalEdge() {
            return original;
        }

        /**
         * Returns the source of the mirror edge.
         *
         * @return the source.
         */
        public Node source() {
            return source;
        }

        /**
         * Returns the target of the mirror edge.
         *
         * @return the target.
         */
        public Node target() {
            return target;
        }

        /**
         * Returns the bends of the mirror edge.
         *
         * @return the bends.
         */
        public List<Node> bends() {
            return bends;
        }

        /**
         * Returns the segments of the mirror edge.
         *
         * @return the segments.
         */
        public List<Edge> segments() {
            return segments;
        }
    }

    /**
     * Collects the information of a node attribute to preserve.
     */
    private static class NodeAttributeToPreserve {

        String id;
        boolean applySourceToBends;

        public NodeAttributeToPreserve(String attrId, boolean applySourceToBends) {
            this.id = attrId;
            this.applySourceToBends = applySourceToBends;
        }
    }
}
