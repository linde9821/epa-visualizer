╔═ must create correct dot export ═╗
digraph EPA {
    rankdir=LR;
    // states (nodes)
    "-955356007" [label="Root"];
    "448734952" [label="a 1\na 2\na 3\na 4"];
    "1025881722" [label="b 1\nb 4"];
    "1737562409" [label="c 1\nc 4"];
    "-1970140069" [label="d 1\nd 4"];
    "-944799893" [label="f 1"];
    "775974490" [label="e 1"];
    "-944799894" [label="e 4"];
    "1025881723" [label="c 2\nc 3"];
    "1737562439" [label="b 2\nb 3"];
    "-1970139139" [label="d 2\nd 3"];
    "-944771063" [label="f 2"];
    "776868220" [label="e 2"];
    "-944771064" [label="e 3"];
    // transitions
    "-955356007" -> "448734952" [label="a"];
    "448734952" -> "1025881722" [label="b"];
    "448734952" -> "1025881723" [label="c"];
    "1025881722" -> "1737562409" [label="c"];
    "1737562409" -> "-1970140069" [label="d"];
    "-1970140069" -> "-944799893" [label="f"];
    "-1970140069" -> "-944799894" [label="e"];
    "-944799893" -> "775974490" [label="e"];
    "1025881723" -> "1737562439" [label="b"];
    "1737562439" -> "-1970139139" [label="d"];
    "-1970139139" -> "-944771063" [label="f"];
    "-1970139139" -> "-944771064" [label="e"];
    "-944771063" -> "776868220" [label="e"];
    // partitions
    subgraph cluster_partition0 {
        label = "Partition 0";
        color=black;
        "-955356007"
    }
    subgraph cluster_partition1 {
        label = "Partition 1";
        color=black;
        "448734952"
        "1025881722"
        "1737562409"
        "-1970140069"
        "-944799893"
        "775974490"
    }
    subgraph cluster_partition4 {
        label = "Partition 4";
        color=black;
        "-944799894"
    }
    subgraph cluster_partition2 {
        label = "Partition 2";
        color=black;
        "1025881723"
        "1737562439"
        "-1970139139"
        "-944771063"
        "776868220"
    }
    subgraph cluster_partition3 {
        label = "Partition 3";
        color=black;
        "-944771064"
    }
}

╔═ must create correct dot with breath first visit ═╗
digraph EPA {
    rankdir=LR;
    // states (nodes)
    "-955356007" [label="Root"];
    "448734952" [label="a 1\na 2\na 3\na 4"];
    "1025881722" [label="b 1\nb 4"];
    "1025881723" [label="c 2\nc 3"];
    "1737562409" [label="c 1\nc 4"];
    "1737562439" [label="b 2\nb 3"];
    "-1970140069" [label="d 1\nd 4"];
    "-1970139139" [label="d 2\nd 3"];
    "-944799893" [label="f 1"];
    "-944799894" [label="e 4"];
    "-944771063" [label="f 2"];
    "-944771064" [label="e 3"];
    "775974490" [label="e 1"];
    "776868220" [label="e 2"];
    // transitions
    "-955356007" -> "448734952" [label="a"];
    "448734952" -> "1025881722" [label="b"];
    "448734952" -> "1025881723" [label="c"];
    "1025881722" -> "1737562409" [label="c"];
    "1025881723" -> "1737562439" [label="b"];
    "1737562409" -> "-1970140069" [label="d"];
    "1737562439" -> "-1970139139" [label="d"];
    "-1970140069" -> "-944799893" [label="f"];
    "-1970140069" -> "-944799894" [label="e"];
    "-1970139139" -> "-944771063" [label="f"];
    "-1970139139" -> "-944771064" [label="e"];
    "-944799893" -> "775974490" [label="e"];
    "-944771063" -> "776868220" [label="e"];
    // partitions
    subgraph cluster_partition0 {
        label = "Partition 0";
        color=black;
        "-955356007"
    }
    subgraph cluster_partition1 {
        label = "Partition 1";
        color=black;
        "448734952"
        "1025881722"
        "1737562409"
        "-1970140069"
        "-944799893"
        "775974490"
    }
    subgraph cluster_partition2 {
        label = "Partition 2";
        color=black;
        "1025881723"
        "1737562439"
        "-1970139139"
        "-944771063"
        "776868220"
    }
    subgraph cluster_partition4 {
        label = "Partition 4";
        color=black;
        "-944799894"
    }
    subgraph cluster_partition3 {
        label = "Partition 3";
        color=black;
        "-944771064"
    }
}

╔═ [end of file] ═╗
