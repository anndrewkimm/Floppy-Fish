class Bird {
    constructor(image) {
        this.x = 45; // boardWidth / 8
        this.y = 320; // boardHeight / 2
        this.width = 34;
        this.height = 24;
        this.img = image;
        this.velocityY = 0;
        this.gravity = 0.5; // Lower gravity for a slower fall
        this.jumpStrength = 8; // Increase jump strength
    }

    update() {
        this.velocityY += this.gravity;
        this.y += this.velocityY;
        this.y = Math.max(this.y, 0); // Limit bird's position to the top of the canvas
    }

    draw(context) {
        context.drawImage(this.img, this.x, this.y, this.width, this.height);
    }
}

class Pipe {
    constructor(image, x, y) {
        this.x = x;
        this.y = y;
        this.width = 64;
        this.height = 512; // Adjust height as needed
        this.img = image;
        this.passed = false;
    }

    update(velocityX) {
        this.x += velocityX;
    }

    draw(context) {
        context.drawImage(this.img, this.x, this.y, this.width, this.height);
    }
}

class FlappyBird {
    constructor() {
        this.canvas = document.getElementById('gameCanvas');
        this.context = this.canvas.getContext('2d');

        // Load background image
        this.backgroundImg = new Image();
        this.backgroundImg.src = './flappybirdbg.png'; // Your background image

        this.birdImg = new Image();
        this.birdImg.src = './flappybird.png';
        this.topPipeImg = new Image();
        this.topPipeImg.src = './toppipe.png';
        this.bottomPipeImg = new Image();
        this.bottomPipeImg.src = './bottompipe.png';

        this.bird = new Bird(this.birdImg);
        this.pipes = [];
        this.score = 0;
        this.gameOver = false;

        this.pipeInterval = setInterval(() => this.placePipes(), 1500);
        this.gameLoop = setInterval(() => this.update(), 1000 / 60);
        
        window.addEventListener('keydown', (e) => this.handleKey(e));
    }

    drawBackground() {
        this.context.drawImage(this.backgroundImg, 0, 0, this.canvas.width, this.canvas.height);
    }

    placePipes() {
        const openingSpace = 150; // Space between pipes
        const randomPipeY = Math.floor(Math.random() * (this.canvas.height - openingSpace - 150)) + 50; // Adjust limits as needed
        this.pipes.push(new Pipe(this.topPipeImg, this.canvas.width, randomPipeY - 512));
        this.pipes.push(new Pipe(this.bottomPipeImg, this.canvas.width, randomPipeY + openingSpace));
    }

    handleKey(event) {
        if (event.code === 'Space') {
            if (this.gameOver) {
                this.restart();
            } else {
                this.bird.velocityY = -this.bird.jumpStrength; // Use jumpStrength
            }
        }
    }

    restart() {
        this.bird.y = this.canvas.height / 2;
        this.bird.velocityY = 0;
        this.pipes = [];
        this.score = 0;
        this.gameOver = false;
    }

update() {
    if (!this.gameOver) {
        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height); // Clear the canvas
        
        this.bird.update();
        this.bird.draw(this.context);

        // Check if the bird has fallen below the canvas
        if (this.bird.y + this.bird.height >= this.canvas.height) {
            this.gameOver = true;
        }

        this.pipes.forEach((pipe) => {
            pipe.update(-4);
            pipe.draw(this.context);
            
            // Collision detection
            if (this.checkCollision(this.bird, pipe)) {
                this.gameOver = true;
            }
            
            // Update score
            if (!pipe.passed && this.bird.x > pipe.x + pipe.width) {
                this.score += 0.5; // Increment score
                pipe.passed = true;
            }
        });

        // Remove pipes that are out of bounds
        this.pipes = this.pipes.filter(pipe => pipe.x + pipe.width > 0);
        this.drawScore();
    }
}


    checkCollision(bird, pipe) {
        return bird.x < pipe.x + pipe.width &&
               bird.x + bird.width > pipe.x &&
               bird.y < pipe.y + pipe.height &&
               bird.y + bird.height > pipe.y;
    }

    drawScore() {
        this.context.fillStyle = 'white';
        this.context.font = '32px Arial';
        this.context.fillText(`Score: ${Math.floor(this.score)}`, 10, 35);
        if (this.gameOver) {
            this.context.fillText('Game Over!', this.canvas.width / 2 - 80, this.canvas.height / 2);
        }
    }
}

window.onload = () => {
    new FlappyBird();
};
