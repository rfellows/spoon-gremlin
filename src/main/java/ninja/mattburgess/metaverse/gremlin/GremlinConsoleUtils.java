package ninja.mattburgess.metaverse.gremlin;

import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.dictionary.DictionaryHelper;
import org.pentaho.metaverse.api.messages.Messages;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;

/**
 * Created by mburgess on 5/18/15.
 */
public class GremlinConsoleUtils {

  private static final String MESSAGE_PREFIX_NODETYPE = "USER.nodetype.";
  private static final String MESSAGE_PREFIX_LINKTYPE = "USER.linktype.";
  private static final String MESSAGE_PREFIX_CATEGORY = "USER.category.";
  private static final String MESSAGE_FAILED_PREFIX = "!";

  public static FormData createFormData() {
    FormData formData = new FormData();
    formData.left = new FormAttachment( 0, 0 );
    formData.top = new FormAttachment( 0, 0 );
    formData.right = new FormAttachment( 100, 0 );
    formData.bottom = new FormAttachment( 100, 0 );
    return formData;
  }

  /**
   * Enhances a vertex by adding localized type and category, and a suggested color
   *
   * @param vertex The vertex to enhance
   */
  public static void enhanceVertex( Vertex vertex ) {
    String type = vertex.getProperty( DictionaryConst.PROPERTY_TYPE );
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_NODETYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.setProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
    // get the vertex category and set it
    String category = DictionaryHelper.getCategoryForType( type );
    vertex.setProperty( DictionaryConst.PROPERTY_CATEGORY, category );
    // get the vertex category color and set it
    String color = DictionaryHelper.getColorForCategory( category );
    vertex.setProperty( DictionaryConst.PROPERTY_COLOR, color );
    //localize the category
    String localizedCat = Messages.getString( MESSAGE_PREFIX_CATEGORY + category );
    if ( !localizedCat.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      vertex.setProperty( DictionaryConst.PROPERTY_CATEGORY_LOCALIZED, localizedCat );
    }
  }

  /**
   * Enhances an edge by adding a localized type
   *
   * @param edge The edge to enhance
   */
  public static void enhanceEdge( Edge edge ) {
    String type = edge.getLabel();
    //localize the node type
    String localizedType = Messages.getString( MESSAGE_PREFIX_LINKTYPE + type );
    if ( !localizedType.startsWith( MESSAGE_FAILED_PREFIX ) ) {
      edge.setProperty( DictionaryConst.PROPERTY_TYPE_LOCALIZED, localizedType );
    }
  }

  public static boolean displayVertex( Vertex v ) {
    return v != null
      && !DictionaryConst.NODE_TYPE_ENTITY.equals( v.getProperty( "type" ) )
      && !DictionaryConst.NODE_TYPE_LOCATOR.equals( v.getProperty( "type" ) )
      && !DictionaryConst.NODE_TYPE_ROOT_ENTITY.equals( v.getProperty( "type" ) );
  }
}
