grammar Journey;

journey: JOURNEY (setwaypoint | listwaypoints | waypoint | player | server | admin | cancel)? EOF;
journeyto: JOURNEY_TO journeytoTarget? EOF;

setwaypoint: SET_WAYPOINT name=identifier+;
listwaypoints: LIST_WAYPOINTS page=ID?;
waypoint: WAYPOINT name=identifier (unsetWaypoint | renameWaypoint | publicWaypoint | flagSet)?;
unsetWaypoint: UNSET;
renameWaypoint: RENAME newname=identifier;
publicWaypoint: PUBLIC (TRUE | FALSE)?;

player: PLAYER user=identifier (playerWaypoint)?;
playerWaypoint: name=identifier;

server: SERVER (serverSetWaypoint | serverListWaypoints | serverWaypoint);
serverSetWaypoint: SET_WAYPOINT name=identifier;
serverListWaypoints: LIST_WAYPOINTS page=ID?;
serverWaypoint: WAYPOINT name=identifier (unsetServerWaypoint | renameServerWaypoint | flagSet)?;
unsetServerWaypoint: UNSET name=identifier;
renameServerWaypoint: RENAME newname=identifier;

admin: ADMIN (debug | invalidate=INVALIDATE | reload=RELOAD | LIST_NETHER_PORTALS);
debug: DEBUG target=identifier?;
cancel: CANCEL;

journeytoTarget: (identifier COLON)* identifier flagSet?;

flagSet: (timeoutFlag | animateFlag | noflyFlag | nodoorFlag | digFlag)+;
timeoutFlag: FLAG_TIMEOUT EQUAL timeout=ID;
animateFlag: FLAG_ANIMATE (EQUAL delay=ID)?;
noflyFlag: FLAG_NO_FLY;
nodoorFlag: FLAG_NO_DOOR;
digFlag: FLAG_DIG;

ADMIN: 'admin';
CANCEL: 'cancel';
COLON: ':';
DEBUG: 'debug';
FALSE: 'false';
INVALIDATE: 'invalidate';
JOURNEY: 'journey';
JOURNEY_TO: 'journeyto';
LIST_NETHER_PORTALS: 'listnetherportals';
LIST_WAYPOINTS: 'listwaypoints';
PATH: 'path';
PLAYER: 'player';
PUBLIC: 'public';
RELOAD: 'reload';
RENAME: 'rename';
SERVER: 'server';
SET: 'set';
SET_WAYPOINT: 'setwaypoint';
TRUE: 'true';
UNSET: 'unset';
WAYPOINT: 'waypoint';

FLAG_ANIMATE: '-animate';
FLAG_DIG: '-dig';
FLAG_NO_DOOR: '-nodoor';
FLAG_NO_FLY: '-nofly';
FLAG_TIMEOUT: '-timeout';

EQUAL: '=';

// MANTLE NODES
identifier: ident | SINGLE_QUOTE ident+ SINGLE_QUOTE | DOUBLE_QUOTE ident+ DOUBLE_QUOTE;
ident: ID
        | ADMIN
        | CANCEL
        | DEBUG
        | FALSE
        | INVALIDATE
        | JOURNEY
        | JOURNEY_TO
        | LIST_NETHER_PORTALS
        | LIST_WAYPOINTS
        | PATH
        | PLAYER
        | PUBLIC
        | RELOAD
        | RENAME
        | SERVER
        | SET
        | SET_WAYPOINT
        | TRUE
        | UNSET
        | WAYPOINT;
ID: [a-zA-Z0-9\-_]+;
ID_SET: ID (COMMA ID)+;
COMMA: ',';
SINGLE_QUOTE: '\'';
DOUBLE_QUOTE: '"';
WS : [ \t\r\n]+ -> channel(HIDDEN); // skip spaces, tabs, newlines
// END MANTLE NODES