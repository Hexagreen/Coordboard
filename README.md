# Coordboard

A lightweight NeoForge addon for **Create Aeronautics** that allows a **Navigation Table** to read navigation coordinates directly from a **Create Clipboard**.

## Features

* Use a Clipboard as a Navigation Table target.
* Read coordinates from checked Clipboard entries.
* Supports multiple coordinate formats.
* Uses the **top-most checked entry** on the **last opened page**.
* Server-side only.

## Requirements

| Component          | Version    |
| ------------------ | ---------- |
| Minecraft          | 1.21.1     |
| Create             | 6.0.10+    |
| Create Aeronautics | 1.2.1+     |

## Usage

1. Write one or more coordinate entries on a Clipboard.
2. Check the coordinate you wish to navigate to.
3. Ensure it is on the last Clipboard page you opened.
4. Insert the Clipboard into a Navigation Table.

The Navigation Table will recognize the selected coordinate as its navigation target.

## Supported Coordinate Formats

### Labeled

```text
X: 120 Z: -450
```

```text
x=120, z=-450
```

### X Z

```text
120 -450
```

```text
120, -450
```

### X Y Z

```text
120 64 -450
```

```text
120, 64, -450
```

> The Y coordinate is ignored.

## Limitations

* Only integer coordinates are supported.
* Y values are ignored.
* Only checked entries are considered.
* If multiple entries are checked, the top-most checked entry on the last opened page is used.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Issues

If you encounter a bug or have a feature request, please open an issue on GitHub.
