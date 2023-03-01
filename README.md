# RevoluteJointTest

Run the application and rotate the mouse slowly around the center of the Box2D shape. The wheels should follow the angle of the pointer (relative to the center).

The issue appears when crossing the -PI / PI boundary (south), the outside wheels will start bouncing and never follow their intended rotation. The central wheel has a different behavior and will rotate correctly the long way around its joint.
