package com.pentaho.metaverse.gremlin.views;

import com.pentaho.metaverse.gremlin.event.GremlinConsoleEvent;
import com.pentaho.metaverse.gremlin.event.GremlinConsoleEventListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mburgess on 5/18/15.
 */
public abstract class BaseGremlinConsoleComposite extends Composite implements GremlinConsoleEventListener {

  protected List<GremlinConsoleEventListener> listeners;

  public BaseGremlinConsoleComposite( Composite parent, int style ) {
    super( parent, style );
    listeners = new ArrayList<>();
  }

  public void addListener( GremlinConsoleEventListener listener ) {
    listeners.add( listener );
  }

  public void removeListener( GremlinConsoleEventListener listener ) {
    listeners.remove( listener );
  }

  public void notifyListeners( Object o ) {
    // Notify listeners
    for ( GremlinConsoleEventListener listener : listeners ) {
      GremlinConsoleEvent gremlinConsoleEvent = new GremlinConsoleEvent( this, o );
      listener.handleGremlinConsoleEvent( gremlinConsoleEvent );
    }
  }

  public void handleGremlinConsoleEvent( GremlinConsoleEvent event ) {
    if ( event.getSource() != this ) {
      setData( event.getData() );
    }
  }

  // TODO make abstract
  public void clear() {}
}
