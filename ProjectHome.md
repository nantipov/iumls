**Incredibly Uncomplicated Language Stuff** is a java library that provides a small programming language itself, grammar parser and computing machine. It might be used for implementing flexible configurations, arithmetic expressions computation or even as a script interpreting solution.


Small notation:

```
#shop {
    fruits = [
        "orange",
        ["apple", "pine" + "apple"]
    ];
    other_goods = [
        "boots",
        #bargain {
            cash = -1 + 2001;
        }
    ];
}
```

how much?
```

                // parse
                Parser p = new Parser("......"); // notation text here
                p.parse();
                // get bargain cash
                Expression exp = (Expression) p.getRootSection().get(1).get(1).get(0);
                // compute
                Computer c = new Computer();
                VariableValue value = c.compute(exp);
                System.out.println(value.asString());
```

will print

```
2000
```