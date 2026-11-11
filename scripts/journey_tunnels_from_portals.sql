-- Journey tunnels generated from WHIMC-Portals portalData.yml
-- Cross-world links only; same-world portals skipped.
-- tunnel_type 0 = NETHER (standard portal tunnel cost in Journey)

DELETE FROM journey_tunnels;

INSERT INTO journey_tunnels (
  origin_domain_id, origin_x, origin_y, origin_z,
  destination_domain_id, destination_x, destination_y, destination_z,
  tunnel_type
) VALUES
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -1826, 130, 544, UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), -106, 78, 8, 0), -- earth
  (UNHEX('6b6de5701f754ed18657ddb743ff92d4'), -1603, 144, 2911, UNHEX('66029d48aa7e4b258da8525309768a49'), -22, 71, 209, 0), -- RocketMoon
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -1802, 127, 561, UNHEX('6b6de5701f754ed18657ddb743ff92d4'), -1556, 67, 2705, 0), -- Hub_RocketLaunch
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -57, 39, 146, UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), -106, 78, 8, 0), -- EarthControlHub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -103, 39, 146, UNHEX('eb47310156b04f618c428f70eef23acd'), -56, 75, -23, 0), -- NoMoonHub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -91, 53, 212, UNHEX('6b6de5701f754ed18657ddb743ff92d4'), -1556, 67, 2705, 0), -- Shuttle2Rocket
  (UNHEX('66029d48aa7e4b258da8525309768a49'), 177, 53, -151, UNHEX('3c04240d604b438db2d978e715a0a514'), -51, 36, 98, 0), -- Moon2Hub
  (UNHEX('66029d48aa7e4b258da8525309768a49'), 153, 53, -127, UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), 281, 78, 163, 0), -- obstut_Start
  (UNHEX('66029d48aa7e4b258da8525309768a49'), 158, 53, -127, UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), 281, 78, 163, 0), -- obstut_End
  (UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), 369, 79, -225, UNHEX('66029d48aa7e4b258da8525309768a49'), 159, 53, -128, 0), -- obstut_exit
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 39, 44, UNHEX('5b703589cc2749b2b69c1ff55abc1fe4'), 190, 83, 174, 0), -- TiltedWarm
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -134, 38, 98, UNHEX('413f0fdb041c448eaa50d81bb702298b'), -99, 78, 2, 0), -- ColderStrip
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -31, 37, 98, UNHEX('66029d48aa7e4b258da8525309768a49'), -22, 71, 209, 0), -- hub2moon
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -49, 38, 98, UNHEX('66029d48aa7e4b258da8525309768a49'), -23, 71, 209, 0), -- back2moon
  (UNHEX('4efa3f5bf5044051a822952e3076c1b7'), -16, 77, 9, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- trappist2hub
  (UNHEX('010a0de4919f41bc854c3ccb104fd482'), 2, 95, 29, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- gliese2hub
  (UNHEX('f2e010dfe1ad4dd9888614e1537c34ea'), -100, 66, 9, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- cancri2hub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -69, 46, 91, UNHEX('ce136d72019e42e295d8ad2fe842e00c'), -425, 97, -220, 0), -- hub2mars
  (UNHEX('18f5a48cee364f10b1b7f999ed194477'), -91, 76, 29, UNHEX('413f0fdb041c448eaa50d81bb702298b'), -177, 85, -16, 0), -- CSunHotReturn
  (UNHEX('a748906a9b82498e85994563daffb7d9'), -79, 75, 13, UNHEX('413f0fdb041c448eaa50d81bb702298b'), 268, 83, -16, 0), -- CSunColdReturn
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), -101, 81, 3, UNHEX('3c04240d604b438db2d978e715a0a514'), -51, 36, 98, 0), -- ColderSun2hub
  (UNHEX('eb47310156b04f618c428f70eef23acd'), -65, 82, -14, UNHEX('3c04240d604b438db2d978e715a0a514'), -51, 36, 98, 0), -- nomoon2hub
  (UNHEX('7e159c5ba33b46deabb6ed8a4979cbb3'), -116, 80, 8, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 37, 154, 0), -- earthcontrol2hub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -31, 37, 113, UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1329, 63, 949, 0), -- mynoa
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 46, 125, UNHEX('7d2861804b634a69a7d76337d044d56b'), 47, 80, 50, 0), -- hub2cancri
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -74, 46, 124, UNHEX('010a0de4919f41bc854c3ccb104fd482'), 2, 94, 37, 0), -- hub2gliese
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -86, 46, 124, UNHEX('4efa3f5bf5044051a822952e3076c1b7'), -18, 74, 10, 0), -- hub2trappist
  (UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1321, 67, 891, UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), -1, 86, 175, 0), -- mynoaclose2mynoahalf
  (UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), 44, 101, 188, UNHEX('9b62735ea8574de39fb382a3e015fc7d'), 307, 69, 187, 0), -- mynoahalf2mynoamangrove
  (UNHEX('ce136d72019e42e295d8ad2fe842e00c'), -417, 98, -226, UNHEX('3c04240d604b438db2d978e715a0a514'), -60, 45, 91, 0), -- mars2hub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -73, 46, 88, UNHEX('501679dce7aa4ad4ba6efa35da8f0714'), 18, 101, 39, 0), -- hub2life
  (UNHEX('501679dce7aa4ad4ba6efa35da8f0714'), 18, 102, 41, UNHEX('3c04240d604b438db2d978e715a0a514'), -60, 45, 91, 0), -- life2hub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -64, 46, 116, UNHEX('5afc5613a8bd49d68a7382af94db7a32'), 279, 90, -32, 0), -- hub2kepler
  (UNHEX('5afc5613a8bd49d68a7382af94db7a32'), 280, 91, -32, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- kepler2hub
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), -181, 86, -16, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, 0), -- CSunStrip2hot
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), 271, 84, -16, UNHEX('a748906a9b82498e85994563daffb7d9'), -91, 77, -10, 0), -- CSunStrip2cold
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -88, 45, 107, UNHEX('e328f4869fb44375adb1bc85bfaddbd1'), 41, 133, 20, 0), -- hub2browndwarf
  (UNHEX('e328f4869fb44375adb1bc85bfaddbd1'), 40, 133, 23, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- browndwarf2hub
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -31, 37, 83, UNHEX('7a5d8e1791864cc393e33933560213f8'), 31, 65, 75, 0), -- hub2TwoMoons
  (UNHEX('7a5d8e1791864cc393e33933560213f8'), 47, 70, 88, UNHEX('3c04240d604b438db2d978e715a0a514'), -51, 36, 98, 0), -- TwoMoons2hub
  (UNHEX('a716aae657134c7aaedfea1ca49917df'), 47, 70, 87, UNHEX('7a5d8e1791864cc393e33933560213f8'), 31, 65, 75, 0), -- TwoMoonsLow
  (UNHEX('7a5d8e1791864cc393e33933560213f8'), -20, 67, 99, UNHEX('99e7b812ca7b4fd787467a88bd191059'), 70, 86, 80, 0), -- Lluna
  (UNHEX('99e7b812ca7b4fd787467a88bd191059'), 68, 87, 76, UNHEX('7a5d8e1791864cc393e33933560213f8'), 31, 65, 75, 0), -- Return2Moons
  (UNHEX('67c14a3896a74dd1916d51bedfeb9543'), -112, 175, -103, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- quasar2hub
  (UNHEX('a716aae657134c7aaedfea1ca49917df'), -20, 67, 99, UNHEX('99e7b812ca7b4fd787467a88bd191059'), 70, 86, 80, 0), -- Lluna2
  (UNHEX('7d2861804b634a69a7d76337d044d56b'), 47, 81, 42, UNHEX('f2e010dfe1ad4dd9888614e1537c34ea'), -19, 93, 51, 0), -- cancri_hot2CancriCold
  (UNHEX('2b3900c83ea64492939b4ef38dea498d'), -94, 145, 9, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- neutron2hub
  (UNHEX('5d9a0e13c98e41be929bc8fdaf46a6dc'), -3, 156, 205, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- blackhole2hub
  (UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), -44, 91, 183, UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1329, 63, 949, 0), -- mynoahalf2mynoaclose
  (UNHEX('f2e010dfe1ad4dd9888614e1537c34ea'), -20, 68, 32, UNHEX('7d2861804b634a69a7d76337d044d56b'), 47, 80, 50, 0), -- cancricold2cancrihot
  (UNHEX('3c04240d604b438db2d978e715a0a514'), -96, 46, 116, UNHEX('ed631d6f5fa645158deb2d58346ad8ea'), -412, 122, -474, 0), -- hub2icarus
  (UNHEX('ed631d6f5fa645158deb2d58346ad8ea'), -437, 124, -506, UNHEX('501679dce7aa4ad4ba6efa35da8f0714'), 18, 101, 39, 0), -- icarus2etlife
  (UNHEX('ed631d6f5fa645158deb2d58346ad8ea'), -437, 124, -485, UNHEX('3c04240d604b438db2d978e715a0a514'), -80, 45, 115, 0), -- icarus2hub
  (UNHEX('f41fc958304b4507a2296d61598ce6bc'), 105, 186, 520, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- pillars2hub
  (UNHEX('b015cd4a2bc64ff1bb5d1352cd0b705a'), 207, 152, 5, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- supercluster2hub
  (UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 92, 434, UNHEX('8e54c09f14b74fe09df2690c1fcfea82'), 384, 81, -410, 0), -- warpone
  (UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 236, 434, UNHEX('5b703589cc2749b2b69c1ff55abc1fe4'), 190, 83, 174, 0), -- warpzero
  (UNHEX('8e54c09f14b74fe09df2690c1fcfea82'), 189, 83, 180, UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 84, 434, 0), -- tiltedfrozen2melting
  (UNHEX('5b703589cc2749b2b69c1ff55abc1fe4'), 401, 74, -458, UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 231, 434, 0), -- tiltedwarm2frozen
  (UNHEX('5b703589cc2749b2b69c1ff55abc1fe4'), 189, 83, 180, UNHEX('3c04240d604b438db2d978e715a0a514'), -54, 37, 90, 0), -- tiltedwarm2hub
  (UNHEX('7a5d8e1791864cc393e33933560213f8'), -444, 66, 128, UNHEX('a716aae657134c7aaedfea1ca49917df'), 109, 65, 23, 0), -- twomoonstime2twomoonslow
  (UNHEX('4723c9a92d904ab8b4f84e8ffb2a5460'), 242, 156, -4, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- universe2hub
  (UNHEX('77b76da2f7a5422f8cc6d5a64825c594'), -5, 22, 0, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- sagittarius2hub
  (UNHEX('9cf014a66b64476391f303caa9cb5179'), 17, 140, -2416, UNHEX('3c04240d604b438db2d978e715a0a514'), -128, 36, 116, 0), -- solarsystem2hub
  (UNHEX('9b62735ea8574de39fb382a3e015fc7d'), 252, 80, 379, UNHEX('3c04240d604b438db2d978e715a0a514'), -51, 36, 98, 0), -- mynoamangrove-return
  (UNHEX('9b62735ea8574de39fb382a3e015fc7d'), 262, 77, 389, UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1329, 63, 949, 0), -- mynoamangrove-loop
  -- Same-world habitat elevators (portal arrival Y is far above underground NPCs)
  (UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -88, 60, -80, 0), -- ColderHot-surface-to-habitat
  (UNHEX('a748906a9b82498e85994563daffb7d9'), -90, 77, -10, UNHEX('a748906a9b82498e85994563daffb7d9'), -77, 54, -50, 0) -- ColderCold-surface-to-habitat
;