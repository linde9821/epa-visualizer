╔═ must apply filter as expected ═╗
simple2.xescompressed
States:
root,[root] -> a1,[a1] -> b1-d1,[a1] -> c1-e1-f1,[c1-e1-f1] -> f2,[c1-e1-f1] -> e2-c2
Activities:
b1-d1,c1-e1-f1,a1,f2,e2-c2
Transitions:
Transition(start=[root] -> a1, activity=b1-d1, end=[a1] -> b1-d1),Transition(start=[root] -> a1, activity=c1-e1-f1, end=[a1] -> c1-e1-f1),Transition(start=root, activity=a1, end=[root] -> a1),Transition(start=[a1] -> c1-e1-f1, activity=f2, end=[c1-e1-f1] -> f2),Transition(start=[a1] -> c1-e1-f1, activity=e2-c2, end=[c1-e1-f1] -> e2-c2)
Partition by state: State -> C
root:0,[root] -> a1:1,[c1-e1-f1] -> f2:2,[c1-e1-f1] -> e2-c2:3,[a1] -> b1-d1:1,[a1] -> c1-e1-f1:2
Sequence by state: State -> list(Event)
root:,[root] -> a1:Event(activity=a1, timestamp=1745625600000, caseIdentifier=1, predecessorIndex=null, successorIndex=1),Event(activity=a1, timestamp=1745625600000, caseIdentifier=2, predecessorIndex=null, successorIndex=1),Event(activity=a1, timestamp=1745625600000, caseIdentifier=3, predecessorIndex=null, successorIndex=1),[c1-e1-f1] -> f2:Event(activity=f2, timestamp=1745625840000, caseIdentifier=2, predecessorIndex=3, successorIndex=null),[c1-e1-f1] -> e2-c2:Event(activity=e2, timestamp=1745625840000, caseIdentifier=3, predecessorIndex=3, successorIndex=5),Event(activity=c2, timestamp=1745625900000, caseIdentifier=3, predecessorIndex=4, successorIndex=null),[a1] -> b1-d1:Event(activity=b1, timestamp=1745625660000, caseIdentifier=1, predecessorIndex=0, successorIndex=2),Event(activity=d1, timestamp=1745625720000, caseIdentifier=1, predecessorIndex=1, successorIndex=null),[a1] -> c1-e1-f1:Event(activity=c1, timestamp=1745625660000, caseIdentifier=2, predecessorIndex=0, successorIndex=2),Event(activity=c1, timestamp=1745625660000, caseIdentifier=3, predecessorIndex=0, successorIndex=2),Event(activity=e1, timestamp=1745625720000, caseIdentifier=2, predecessorIndex=1, successorIndex=3),Event(activity=e1, timestamp=1745625720000, caseIdentifier=3, predecessorIndex=1, successorIndex=3),Event(activity=f1, timestamp=1745625780000, caseIdentifier=2, predecessorIndex=2, successorIndex=4),Event(activity=f1, timestamp=1745625780000, caseIdentifier=3, predecessorIndex=2, successorIndex=4)

╔═ [end of file] ═╗
