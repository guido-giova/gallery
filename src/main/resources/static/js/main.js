import { initDropZone } from './drop-zone.js';
import { initSizeSlider } from './slider.js';
import { loadImages } from './api.js';

initDropZone();
initSizeSlider();
await loadImages();
