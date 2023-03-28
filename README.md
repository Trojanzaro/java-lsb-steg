# java-lsb-steg
A 32 Bit Bitmap LSB steganography implementation in Java

Any image of `X * Y` pixels can be represented as a 32 bit bitmap

### Pixel:
```
 A(lpha)   R(ed)   G(reen)  B(lue)
10010101 01101010 01010110 01011001
```

Since we can nibble a nibble into two bits
```
byte: 01100110
nibble: 0110 0110
nibble nibble: 01 10 01 10
```

we can replace the last two bits of every channel, (red green blue and alpha) with the bits of a single byte. If we do that for every byte of a single file we can hide a file inside an image so long as the image has a capacity of:

`
X * Y bytes
`

To compile:
```
javac *.java
```

To hide a file into an image:
```
java MainClass -h
```

To extract hidden info from file:
```
java MainClass -e
```

Since all of the data required to extract the information can be abstracted the program can be altered to retrieve the size of the hidden info another way
