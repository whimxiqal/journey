grammar Journey;

journey: JOURNEY (setwaypoint | listwaypoints | waypoint | player | server | admin | surface | death | cancel)? EOF;
journeyto: JOURNEY_TO journeytoTarget EOF;

setwaypoint: SET_WAYPOINT name=identifier+;
listwaypoints: LIST_WAYPOINTS page=ID?;
waypoint: WAYPOINT name=identifier (unsetWaypoint | renameWaypoint | publicWaypoint | flagSet)?;
unsetWaypoint: UNSET;
renameWaypoint: RENAME newname=identifier+;
publicWaypoint: PUBLIC (TRUE | FALSE)?;

player: PLAYER user=identifier (playerWaypoint)?;
playerWaypoint: WAYPOINT name=identifier+;

server: SERVER (serverSetWaypoint | serverListWaypoints | serverWaypoint);
serverSetWaypoint: SET_WAYPOINT name=identifier+;
serverListWaypoints: LIST_WAYPOINTS page=ID?;
serverWaypoint: WAYPOINT name=identifier (unsetServerWaypoint | renameServerWaypoint | flagSet)?;
unsetServerWaypoint: UNSET name=identifier+;
renameServerWaypoint: RENAME name=identifier+;

admin: ADMIN (debug | invalidate=INVALIDATE | reload=RELOAD | LIST_NETHER_PORTALS);
debug: DEBUG target=identifier?;
surface: SURFACE;
death: DEATH;
cancel: CANCEL;

journeytoTarget: scopes ID+ flagSet?;
scopes: (SERVER COLON)? (ID COLON)*;

flagSet: (timeoutFlag | animateFlag | noflyFlag | nodoorFlag | digFlag)+;
timeoutFlag: FLAG_TIMEOUT EQUAL timeout=ID;
animateFlag: FLAG_ANIMATE (EQUAL delay=ID)?;
noflyFlag: FLAG_NO_FLY;
nodoorFlag: FLAG_NO_DOOR;
digFlag: FLAG_DIG;

JOURNEY: 'journey';
JOURNEY_TO: 'journeyto';
ADMIN: 'admin';
LIST_NETHER_PORTALS: 'listnetherportals';
SURFACE: 'surface';
DEATH: 'death';
CANCEL: 'cancel';
INVALIDATE: 'invalidate';
RELOAD: 'reload';
DEBUG: 'debug';
SERVER: 'server';
PUBLIC: 'public';
PATH: 'path';
SET: 'set';
UNSET: 'unset';
RENAME: 'rename';
SET_WAYPOINT: 'setwaypoint';
LIST_WAYPOINTS: 'listwaypoints';
WAYPOINT: 'waypoint';
PLAYER: 'player';
TRUE: 'true';
FALSE: 'false';
COLON: ':';

FLAG_TIMEOUT: '-timeout';
FLAG_ANIMATE: '-animate';
FLAG_NO_DOOR: '-nodoor';
FLAG_NO_FLY: '-nofly';
FLAG_DIG: '-dig';

EQUAL: '=';

// MANTLE NODES
identifier: ID | SINGLE_QUOTE ID+ SINGLE_QUOTE | DOUBLE_QUOTE ID+ DOUBLE_QUOTE;
ID: [a-zA-Z0-9\-_]+;
ID_SET: ID (COMMA ID)+;
COMMA: ',';
SINGLE_QUOTE: '\'';
DOUBLE_QUOTE: '"';
WS : [ \t\r\n]+ -> channel(HIDDEN); // skip spaces, tabs, newlines
// END MANTLE NODES