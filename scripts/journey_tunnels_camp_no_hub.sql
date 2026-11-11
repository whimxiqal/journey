-- WHIMC camp: cross-world tunnels with Hub links removed (kids cannot return to Hub).
-- Also adds ColderStrip catwalk shortcuts — the strip catwalk cannot be pathfound reliably
-- (narrow structure; A* hits max-path-block-count even for ~16 block walks).
--
-- From debug logs: (-97, 78, 7) -> Pierre at (-85, 46, -91) on ColderHot SUCCEEDS.
-- Failures are all ColderStrip catwalk walks to portal pads.

DELETE FROM journey_tunnels;

INSERT INTO journey_tunnels (
  origin_domain_id, origin_x, origin_y, origin_z,
  destination_domain_id, destination_x, destination_y, destination_z,
  tunnel_type
) VALUES
  -- Colder linked worlds
  (UNHEX('18f5a48cee364f10b1b7f999ed194477'), -91, 76, 29, UNHEX('413f0fdb041c448eaa50d81bb702298b'), -177, 85, -16, 0), -- CSunHotReturn
  (UNHEX('a748906a9b82498e85994563daffb7d9'), -79, 75, 13, UNHEX('413f0fdb041c448eaa50d81bb702298b'), 268, 83, -16, 0), -- CSunColdReturn
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), -181, 86, -16, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, 0), -- CSunStrip2hot
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), 271, 84, -16, UNHEX('a748906a9b82498e85994563daffb7d9'), -91, 77, -10, 0), -- CSunStrip2cold
  -- Catwalk -> ColderHot surface (bypass unwalkable strip catwalk)
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), 268, 83, -16, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, 0), -- cold-catwalk-to-hotsurface
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), 255, 83, -17, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, 0), -- cold-catwalk-mid-to-hotsurface
  (UNHEX('413f0fdb041c448eaa50d81bb702298b'), -176, 85, -15, UNHEX('18f5a48cee364f10b1b7f999ed194477'), -97, 78, 7, 0), -- hot-catwalk-to-hotsurface
  -- Other worlds (no Hub)
  (UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1321, 67, 891, UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), -1, 86, 175, 0), -- mynoaclose2mynoahalf
  (UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), 44, 101, 188, UNHEX('9b62735ea8574de39fb382a3e015fc7d'), 307, 69, 187, 0), -- mynoahalf2mynoamangrove
  (UNHEX('a716aae657134c7aaedfea1ca49917df'), 47, 70, 87, UNHEX('7a5d8e1791864cc393e33933560213f8'), 31, 65, 75, 0), -- TwoMoonsLow
  (UNHEX('7a5d8e1791864cc393e33933560213f8'), -20, 67, 99, UNHEX('99e7b812ca7b4fd787467a88bd191059'), 70, 86, 80, 0), -- Lluna
  (UNHEX('99e7b812ca7b4fd787467a88bd191059'), 68, 87, 76, UNHEX('7a5d8e1791864cc393e33933560213f8'), 31, 65, 75, 0), -- Return2Moons
  (UNHEX('a716aae657134c7aaedfea1ca49917df'), -20, 67, 99, UNHEX('99e7b812ca7b4fd787467a88bd191059'), 70, 86, 80, 0), -- Lluna2
  (UNHEX('7d2861804b634a69a7d76337d044d56b'), 47, 81, 42, UNHEX('f2e010dfe1ad4dd9888614e1537c34ea'), -19, 93, 51, 0), -- cancri_hot2CancriCold
  (UNHEX('bc7a08138a3c4ed08a195e0e5366de07'), -44, 91, 183, UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1329, 63, 949, 0), -- mynoahalf2mynoaclose
  (UNHEX('f2e010dfe1ad4dd9888614e1537c34ea'), -20, 68, 32, UNHEX('7d2861804b634a69a7d76337d044d56b'), 47, 80, 50, 0), -- cancricold2cancrihot
  (UNHEX('8e54c09f14b74fe09df2690c1fcfea82'), 189, 83, 180, UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 84, 434, 0), -- tiltedfrozen2melting
  (UNHEX('5b703589cc2749b2b69c1ff55abc1fe4'), 401, 74, -458, UNHEX('496e2a662aad45d9a65704ab306428e2'), -1009, 231, 434, 0), -- tiltedwarm2frozen
  (UNHEX('7a5d8e1791864cc393e33933560213f8'), -444, 66, 128, UNHEX('a716aae657134c7aaedfea1ca49917df'), 109, 65, 23, 0), -- twomoonstime2twomoonslow
  (UNHEX('9b62735ea8574de39fb382a3e015fc7d'), 262, 77, 389, UNHEX('4e7bb60fc79f4174a6d019af7afe56f2'), 1329, 63, 949, 0) -- mynoamangrove-loop
;
