![](src/main/resources/app/home/title.png)

I need to build a project for school in Java, and I challenged myself to building a Neural Network from scratch. 
This led to the development of a nn engine, which I plan on expanding [here](https://github.com/c4vxl/jNN) in the future

---

<p align="center">
    <img src="src/main/resources/app/home/logo.png">
</p>

## About
### The Engine:
The core engine introduces a new data type called Tensor, which can be thought of as a multidimensional matrix supporting various data types. It is capable of performing a wide range of mathematical operations, including element-wise computation, matrix multiplication, reshaping, transposing, and more.

##### Core components:
`Module Class`:
The Module class serves as a base for any model or layer that requires serialization or saving.

`Linear Class`:
The Linear class implements linear transformation by performing weight multiplication and adding an optional bias which can booth be learned during training.


### The goal:
The goal of this project was to create a neural network, capable of identifying any kind of handwritten digit on an 28*28 pixel canvas.

---

## Technical details
##### The model consists of:
- 784 inputs (for 28*28 pixels)
- 10 outputs (Numbers from 0-9)
- 2 hidden layers
- 12 neurons per hidden layer

This adds up to a total of `818 neurons` in the entire model.

##### Dataset:
It has been trained on the train split of the [MNIST Dataset](https://yann.lecun.com/exdb/mnist/) (600000 images) 

---

## Insights

<details>
  <summary><strong>Main app</strong></summary>

  ![](.preview_images/main.png)
</details>

<details>
  <summary><strong>Prompt the model</strong></summary>

  ![](.preview_images/predict.png)
</details>

<details>
  <summary><strong>Creating a dataset for training</strong></summary>

  ![](.preview_images/create_dataset.png)
</details>

<details>
  <summary><strong>Benchmarking on a dataset</strong></summary>

![](.preview_images/benchmark.png)
</details>

<details>
  <summary><strong>Training</strong></summary>

  ## Training Menu
  ![](.preview_images/training_main.png)

  ## Configuration of training
  ![](.preview_images/training_config.png)
</details>

<details>
  <summary><strong>Dataset inspector</strong></summary>

  ![](.preview_images/inspector.png)
</details>


# Licence
It's mine, don't copy!
