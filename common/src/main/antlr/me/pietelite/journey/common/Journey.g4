grammar Journey;

journey: JOURNEY (setwaypoint | listwaypoints | waypoint | player | server | admin | quest | surface | death | cancel | accept | EOF);
journeyto: JOURNEY_TO identifier* EOF;

setwaypoint: SET_WAYPOINT name=identifier;
listwaypoints: LIST_WAYPOINTS;
waypoint: WAYPOINT name=identifier (unsetWaypoint | renameWaypoint)?;
unsetWaypoint: UNSET name=identifier;
renameWaypoint: RENAME newname=identifier;

player: PLAYER (playerWaypoint)?;
playerWaypoint: WAYPOINT name=identifier;

server: SERVER (serverSetWaypoint | serverListWaypoints | serverWaypoint);
serverSetWaypoint: SET_WAYPOINT name=identifier;
serverListWaypoints: LIST_WAYPOINTS;
serverWaypoint: WAYPOINT name=identifier (unsetServerWaypoint | renameServerWaypoint)?;
unsetServerWaypoint: UNSET name=identifier;
renameServerWaypoint: RENAME name=identifier;

admin: ADMIN (debug | invalidate=INVALIDATE | reload=RELOAD | LIST_NETHER_PORTALS);
debug: DEBUG target=identifier;
quest: QUEST EOF;
surface: SURFACE EOF;
death: DEATH EOF;
cancel: CANCEL EOF;
accept: ACCEPT EOF;

JOURNEY: 'journey';
JOURNEY_TO: 'journeyto';
ADMIN: 'admin';
LIST_NETHER_PORTALS: 'listnetherportals';
SURFACE: 'surface';
DEATH: 'death';
CANCEL: 'cancel';
ACCEPT: 'accept';
INVALIDATE: 'invalidate';
RELOAD: 'reload';
DEBUG: 'debug';
PERSONAL: 'personal';
SERVER: 'server';
PRIVATE: 'private';
PUBLIC: 'public';
PATH: 'path';
SET: 'set';
UNSET: 'unset';
RENAME: 'rename';
QUEST: 'quest';
SET_WAYPOINT: 'setwaypoint';
LIST_WAYPOINTS: 'listwaypoints';
WAYPOINT: 'waypoint';
PLAYER: 'player';

// MANTLE NODES
identifier: ID | SINGLE_QUOTE ID SINGLE_QUOTE | DOUBLE_QUOTE ID DOUBLE_QUOTE;
ID: [a-zA-Z0-9\-_]+;
ID_SET: ID (COMMA ID)+;
COMMA: ',';
SINGLE_QUOTE: '\'';
DOUBLE_QUOTE: '"';
WS : [ \t\r\n]+ -> channel(HIDDEN); // skip spaces, tabs, newlines
// END MANTLE NODES