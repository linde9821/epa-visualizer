╔═ partition filter must not remove state if its a parent of remaining partition ═╗
partition_complex_scenario.xes Partition Frequency Filter with threshold >= 0.41
States:
[a] -> c,[root] -> a,root
Activities:
c,a
Transitions:
Transition(start=root, activity=a, end=[root] -> a),Transition(start=[root] -> a, activity=c, end=[a] -> c)
Partition by state: State -> C
[a] -> c:2,[root] -> a:1,root:0
Sequence by state: State -> list(Event)
[a] -> c:Event(activity=c, timestamp=1745625900000, caseIdentifier=3),Event(activity=c, timestamp=1745626020000, caseIdentifier=4),Event(activity=c, timestamp=1745626140000, caseIdentifier=5),[root] -> a:Event(activity=a, timestamp=1745625600000, caseIdentifier=1),Event(activity=a, timestamp=1745625720000, caseIdentifier=2),Event(activity=a, timestamp=1745625840000, caseIdentifier=3),Event(activity=a, timestamp=1745625960000, caseIdentifier=4),Event(activity=a, timestamp=1745626080000, caseIdentifier=5),root:

╔═ [end of file] ═╗
