// Available effects from photon-rs
const effects = [
    'oceanic', 'islands', 'marine', 'seagreen', 'flagblue', 'liquid', 'diamante',
    'radio', 'twenties', 'rosetint', 'mauve', 'bluechrome', 'vintage', 'perfume',
    'serenity', 'golden', 'pastel_pink', 'cali', 'dramatic', 'firenze', 'obsidian',
    'lofi', 'sepia'
];

// Available transformations from photon-rs
const transformations = [
    'fliph', 'flipv', 'resize', 'crop', 'padding_uniform',
    'padding_left', 'padding_right', 'padding_top', 'padding_bottom'
];

let currentImage = null;
let currentImageFile = null;

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    populateEffects();
    populateTransformations();
    setupEventListeners();
});

// Toggle collapsible sections
function toggleSection(sectionName) {
    const content = document.getElementById(sectionName + '-content');
    const title = content.previousElementSibling;
    
    if (content.classList.contains('collapsed')) {
        content.classList.remove('collapsed');
        title.classList.remove('collapsed');
    } else {
        content.classList.add('collapsed');
        title.classList.add('collapsed');
    }
}

function populateEffects() {
    const effectsGrid = document.getElementById('effectsGrid');
    effects.forEach(effect => {
        const button = document.createElement('button');
        button.className = 'effect-button';
        button.textContent = effect.replace(/_/g, ' ');
        button.onclick = () => applyEffect(effect);
        effectsGrid.appendChild(button);
    });
}

function populateTransformations() {
    const transformsGrid = document.getElementById('transformsGrid');
    transformations.forEach(transform => {
        const button = document.createElement('button');
        button.className = 'transform-button';
        button.textContent = transform.replace(/_/g, ' ');
        button.onclick = () => applyTransformation(transform);
        transformsGrid.appendChild(button);
    });
}

function setupEventListeners() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');

    // Click to upload
    uploadArea.addEventListener('click', () => fileInput.click());

    // File input change
    fileInput.addEventListener('change', handleFileSelect);

    // Drag and drop
    uploadArea.addEventListener('dragover', handleDragOver);
    uploadArea.addEventListener('dragleave', handleDragLeave);
    uploadArea.addEventListener('drop', handleDrop);
}

function handleDragOver(e) {
    e.preventDefault();
    e.currentTarget.classList.add('dragover');
}

function handleDragLeave(e) {
    e.preventDefault();
    e.currentTarget.classList.remove('dragover');
}

function handleDrop(e) {
    e.preventDefault();
    e.currentTarget.classList.remove('dragover');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
}

function handleFileSelect(e) {
    const file = e.target.files[0];
    if (file) {
        handleFile(file);
    }
}

async function handleFile(file) {
    if (!file.type.startsWith('image/')) {
        showError('Please select a valid image file.');
        return;
    }

    try {
        // Upload the image to the server
        await uploadImage(file);
        
        // Show image preview
        const reader = new FileReader();
        reader.onload = function(e) {
            currentImage = e.target.result;
            currentImageFile = file;
            
            const imagePreview = document.getElementById('imagePreview');
            const imageContainer = document.getElementById('imageContainer');
            
            imagePreview.src = currentImage;
            imageContainer.style.display = 'block';
            
            // Hide upload area
            document.getElementById('uploadArea').style.display = 'none';
            
            showSuccess('Image uploaded successfully! Choose an effect or transformation to apply.');
        };
        reader.readAsDataURL(file);
        
    } catch (error) {
        console.error('Error uploading image:', error);
        showError(`Failed to upload image: ${error.message}`);
    }
}

async function uploadImage(file) {
    const response = await fetch('/photon/upload', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/octet-stream'
        },
        body: file
    });
    
    if (!response.ok) {
        throw new Error(`Upload failed: ${response.status}`);
    }
}

async function applyEffect(effectName) {
    if (!currentImageFile) {
        showError('Please upload an image first.');
        return;
    }

    showLoading(true);
    hideMessages();

    try {
        const response = await fetch(`/photon/effect?effect=${encodeURIComponent(effectName)}`, {
            method: 'GET'
        });

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        // Get the processed image as blob
        const blob = await response.blob();
        const imageUrl = URL.createObjectURL(blob);
        
        // Update the image preview
        const imagePreview = document.getElementById('imagePreview');
        imagePreview.src = imageUrl;
        
        showSuccess(`Applied ${effectName} effect successfully!`);
    } catch (error) {
        console.error('Error applying effect:', error);
        showError(`Failed to apply effect: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

async function applyTransformation(transformName) {
    if (!currentImageFile) {
        showError('Please upload an image first.');
        return;
    }

    showLoading(true);
    hideMessages();

    try {
        const response = await fetch(`/photon/transform?transformation=${encodeURIComponent(transformName)}`, {
            method: 'GET'
        });

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        // Get the processed image as blob
        const blob = await response.blob();
        const imageUrl = URL.createObjectURL(blob);
        
        // Update the image preview
        const imagePreview = document.getElementById('imagePreview');
        imagePreview.src = imageUrl;
        
        showSuccess(`Applied ${transformName} transformation successfully!`);
    } catch (error) {
        console.error('Error applying transformation:', error);
        showError(`Failed to apply transformation: ${error.message}`);
    } finally {
        showLoading(false);
    }
}

function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
    
    // Disable all buttons while loading
    const buttons = document.querySelectorAll('.effect-button, .transform-button');
    buttons.forEach(button => {
        button.disabled = show;
    });
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}

function showSuccess(message) {
    const successDiv = document.getElementById('successMessage');
    successDiv.textContent = message;
    successDiv.style.display = 'block';
}

function hideMessages() {
    document.getElementById('errorMessage').style.display = 'none';
    document.getElementById('successMessage').style.display = 'none';
}
