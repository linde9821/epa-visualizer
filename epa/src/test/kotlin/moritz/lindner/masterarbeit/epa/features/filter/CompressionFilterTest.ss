╔═ must apply filter as expected ═╗
simple2.xescompressed
States:
root,[root] -> a1,[a1] -> b1d1,[a1] -> c1e1f1,[c1e1f1] -> f2,[c1e1f1] -> e2c2
Activities:
b1d1,c1e1f1,a1,f2,e2c2
Transitions:
Transition(start=[root] -> a1, activity=b1d1, end=[a1] -> b1d1),Transition(start=[root] -> a1, activity=c1e1f1, end=[a1] -> c1e1f1),Transition(start=root, activity=a1, end=[root] -> a1),Transition(start=[a1] -> c1e1f1, activity=f2, end=[c1e1f1] -> f2),Transition(start=[a1] -> c1e1f1, activity=e2c2, end=[c1e1f1] -> e2c2)
Partition by state: State -> C
root:0,[root] -> a1:1,[c1e1f1] -> f2:2,[c1e1f1] -> e2c2:3,[a1] -> b1d1:1,[a1] -> c1e1f1:2
Sequence by state: State -> list(Event)
root:,[root] -> a1:Event(activity=a1, timestamp=1745625600000, caseIdentifier=1, predecessorIndex=null, successorIndex=1),Event(activity=a1, timestamp=1745625600000, caseIdentifier=2, predecessorIndex=null, successorIndex=4),Event(activity=a1, timestamp=1745625600000, caseIdentifier=3, predecessorIndex=null, successorIndex=9),[c1e1f1] -> f2:Event(activity=f2, timestamp=1745625840000, caseIdentifier=2, predecessorIndex=10, successorIndex=null),[c1e1f1] -> e2c2:Event(activity=e2, timestamp=1745625840000, caseIdentifier=3, predecessorIndex=15, successorIndex=17),Event(activity=c2, timestamp=1745625900000, caseIdentifier=3, predecessorIndex=17, successorIndex=null),[a1] -> b1d1:Event(activity=b1, timestamp=1745625660000, caseIdentifier=1, predecessorIndex=1, successorIndex=3),Event(activity=d1, timestamp=1745625720000, caseIdentifier=1, predecessorIndex=3, successorIndex=null),[a1] -> c1e1f1:Event(activity=c1, timestamp=1745625660000, caseIdentifier=2, predecessorIndex=4, successorIndex=6),Event(activity=c1, timestamp=1745625660000, caseIdentifier=3, predecessorIndex=9, successorIndex=11),Event(activity=e1, timestamp=1745625720000, caseIdentifier=2, predecessorIndex=6, successorIndex=8),Event(activity=e1, timestamp=1745625720000, caseIdentifier=3, predecessorIndex=11, successorIndex=13),Event(activity=f1, timestamp=1745625780000, caseIdentifier=2, predecessorIndex=8, successorIndex=10),Event(activity=f1, timestamp=1745625780000, caseIdentifier=3, predecessorIndex=13, successorIndex=15)

╔═ [end of file] ═╗
