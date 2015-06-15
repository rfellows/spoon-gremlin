package ninja.mattburgess.metaverse.gremlin.event;


import org.eclipse.swt.widgets.Event;

/**
 * Created by mburgess on 5/18/15.
 */
public class GremlinConsoleEvent extends Event {

  private Object source;
  private Object data;


  public GremlinConsoleEvent( Object source, Object data ) {
    this.source = source;
    this.data = data;
  }

  public Object getSource() {
    return source;
  }

  public Object getData() {
    return data;
  }
}
