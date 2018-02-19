print(_VERSION)    --> Lua 5.3
print(_G._VERSION) --> Lua 5.3
print(_G)          --> 0x7fce7e402710
print(_G._G)       --> 0x7fce7e402710
print(print)       --> 0x1073e2b90
print(_G.print)    --> 0x1073e2b90

print(select(1, "a", "b", "c"))		--> a    b    c
print(select(2, "a", "b", "c"))		--> b    c
print(select(3, "a", "b", "c"))		--> c
print(select(-1, "a", "b", "c"))	--> c
print(select("#", "a", "b", "c"))	--> 3
