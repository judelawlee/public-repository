package com.feng.arangodb.graph;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.core.IsNot.not;
import static java.util.stream.Stream.empty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ArangoEdgeCollectionTest extends BaseTest {

    private static final String GRAPH_NAME = "db_collection_test";
    private static final String EDGE_COLLECTION_NAME = "db_edge_collection_test";
    private static final String VERTEX_COLLECTION_NAME = "db_vertex_collection_test";

    public ArangoEdgeCollectionTest(final ArangoDB.Builder builder) {
        super(builder);
        setup();
    }

    public void setup() {
        try {
            db.createCollection(VERTEX_COLLECTION_NAME, null);
        } catch (final ArangoDBException e) {
        }
        try {
            db.createCollection(EDGE_COLLECTION_NAME, new CollectionCreateOptions().type(CollectionType.EDGES));
        } catch (final ArangoDBException e) {
        }
        final Collection<EdgeDefinition> edgeDefinitions = new ArrayList<EdgeDefinition>();
        edgeDefinitions.add(new EdgeDefinition().collection(EDGE_COLLECTION_NAME).from(VERTEX_COLLECTION_NAME)
                .to(VERTEX_COLLECTION_NAME));
        db.createGraph(GRAPH_NAME, edgeDefinitions, null);
    }

    @After
    public void teardown() {
        for (final String collection : new String[] { VERTEX_COLLECTION_NAME, EDGE_COLLECTION_NAME }) {
            db.collection(collection).truncate();
        }
    }

    private BaseEdgeDocument createEdgeValue() {
        final VertexEntity v1 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null);
        final VertexEntity v2 = db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME)
                .insertVertex(new BaseDocument(), null);

        final BaseEdgeDocument value = new BaseEdgeDocument();
        value.setFrom(v1.getId());
        value.setTo(v2.getId());
        return value;
    }

    @Test
    public void insertEdge() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        assertThat(edge, is(notNullValue()));
        final BaseEdgeDocument document = db.collection(EDGE_COLLECTION_NAME).getDocument(edge.getKey(),
                BaseEdgeDocument.class, null);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
        assertThat(document.getFrom(), is(notNullValue()));
        assertThat(document.getTo(), is(notNullValue()));
    }

    @Test
    public void insertEdgeUpdateRev() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        assertThat(value.getRevision(), is(edge.getRev()));
    }

    @Test
    public void getEdge() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        final BaseEdgeDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(edge.getKey(), BaseEdgeDocument.class, null);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
        assertThat(document.getFrom(), is(notNullValue()));
        assertThat(document.getTo(), is(notNullValue()));
    }

    @Test
    public void getEdgeIfMatch() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(edge.getRev());
        final BaseDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfMatchFail() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        final BaseEdgeDocument edge2 = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2, is(nullValue()));
    }

    @Test
    public void getEdgeIfNoneMatch() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        final BaseDocument document = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).getEdge(edge.getKey(),
                BaseDocument.class, options);
        assertThat(document, is(notNullValue()));
        assertThat(document.getKey(), is(edge.getKey()));
    }

    @Test
    public void getEdgeIfNoneMatchFail() {
        final BaseEdgeDocument value = createEdgeValue();
        final EdgeEntity edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(value, null);
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(edge.getRev());
        final BaseEdgeDocument edge2 = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).getEdge(edge.getKey(),
                BaseEdgeDocument.class, options);
        assertThat(edge2, is(nullValue()));
    }

    @Test
    public void replaceEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeUpdateEntity replaceResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .replaceEdge(createResult.getKey(), doc, null);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
//        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeUpdateRev() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final EdgeUpdateEntity replaceResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .replaceEdge(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(replaceResult.getRev()));
    }

    @Test
    public void replaceEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity replaceResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .replaceEdge(createResult.getKey(), doc, options);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
//        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        try {
            final EdgeReplaceOptions options = new EdgeReplaceOptions().ifMatch("no");
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).replaceEdge(createResult.getKey(), doc, options);
            fail();
        } catch (final ArangoDBException e) {
        }
    }

    @Test
    public void updateEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, null);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
//        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeUpdateRev() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(updateResult.getRev()));
    }

    @Test
    public void updateEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch(createResult.getRev());
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
//        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        try {
            final EdgeUpdateOptions options = new EdgeUpdateOptions().ifMatch("no");
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).updateEdge(createResult.getKey(), doc, options);
            fail();
        } catch (final ArangoDBException e) {
        }
    }

    @Test
    public void updateEdgeKeepNullTrue() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(true);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet().size(), is(1));
//        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateEdgeKeepNullFalse() {
        final BaseEdgeDocument doc = createEdgeValue();
        doc.addAttribute("a", "test");
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        doc.updateAttribute("a", null);
        final EdgeUpdateOptions options = new EdgeUpdateOptions().keepNull(false);
        final EdgeUpdateEntity updateResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .updateEdge(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseEdgeDocument readResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getRevision(), is(notNullValue()));
//        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
    }

    @Test
    public void deleteEdge() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), null);
        final BaseEdgeDocument edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(edge, is(nullValue()));
    }

    @Test
    public void deleteEdgeIfMatch() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch(createResult.getRev());
        db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), options);
        final BaseEdgeDocument edge = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME)
                .getEdge(createResult.getKey(), BaseEdgeDocument.class, null);
        assertThat(edge, is(nullValue()));

    }

    @Test
    public void deleteEdgeIfMatchFail() {
        final BaseEdgeDocument doc = createEdgeValue();
        final EdgeEntity createResult = db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(doc, null);
        final EdgeDeleteOptions options = new EdgeDeleteOptions().ifMatch("no");
        try {
            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).deleteEdge(createResult.getKey(), options);
            fail();
        } catch (final ArangoDBException e) {
        }
    }
}
