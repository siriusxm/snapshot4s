---
sidebar_position: 10
title: FAQ
---

# Frequently asked questions

## What is the difference between `snapshot4s` and `circe-golden`?

[circe-golden](https://github.com/circe/circe-golden) is a library that is specialized for validating circe codecs. It reduces the maintenance burden of testing codecs by automating the generation of test data.

Unlike `circe-golden`, snapshot4s is aimed at broader example-based testing. It supports example-based tests containing any `String` or algebraic data type.

## What other snapshot testing tools exist?

[circe-golden](https://github.com/circe/circe-golden) is the most similar library for Scala. It specializes in validating circe codecs, as described [above](#what-is-the-difference-between-snapshot4s-and-circe-golden). 

Here are a few tools for other languages:
 - The [Jest Javascript testing framework](https://jestjs.io/docs/snapshot-testing) supports snapshot testing.
 - [Insta.rs](https://insta.rs/) is a snapshot testing tool for Rust.
