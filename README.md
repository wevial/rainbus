# RainBus
*Originally created for the rainbow table project for CS4236 - Cryptography Theory at the National University of Singapore.*

RainBus is a rainbow table generator for 3 byte passwords.

What is a rainbow table?
---

Say you have a hash to a password and you'd like to crack it. One very inefficient approach is to do a brute-force attack which attempts to calculate a hash for every password. Another is to have a precomputed lookup table with hashes mapping to a password(s). A table that stored every possible hash and password, however, takes up a whole lot of space. A rainbow table is a practical alternative that takes up more space but less processing than brute-forcing and less space but more processing than a lookup table. It does this by precomputing "chains" of applying hash and a reduce functions on a plaintext many times (200x, 1000x, 100000x, etc) and arriving with a new "reduced" plaintext. These two values are stored in the table with the new plaintext being the key.

To find the password to a hash, first apply a reduce function to obtain a plaintext. If the plaintext matches a key in the rainbow table, then the chain is "inverted" by going through the hash-reduce chain used to generate the key. If the hash which reduces to the key matches the input hash, then the plaintext that produced that final hash is the password. If the hashes do not match, then the initial plaintext (reduced from the input hash) is hashed and then reduced again. This is applied up to the same number times as the chain length.

Rainbow table attacks can be prevented with [salting] (https://en.wikipedia.org/wiki/Salt_(cryptography)).

[Check out Wikipedia's page on rainbow tables for a more comprehensive explanation.] (https://en.wikipedia.org/wiki/Rainbow_table)

Project constraints
---
* Password length: 3 bytes
* Rainbow table size: ~512 KB
* Accuracy: > 45%

*Note: Multiple tables were allowed in order to improve accuracy, which is what I originally did.*

Improvements @ Recurse Center
---
During my time at the Recurse Center, I cleaned up the code and made the rainbow tables generated both faster and more accurate. Accuracy was improved from ~35% per table to ~56%. The average time to invert a hash is over twice as fast, improving from 0.1 seconds to 0.043 seconds.
