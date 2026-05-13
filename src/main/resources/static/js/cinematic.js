document.addEventListener("DOMContentLoaded", () => {
    const footer = document.querySelector(".site-footer");
    const canvas = document.querySelector("[data-scrollytelling-canvas]");
    const loader = document.querySelector("[data-scrollytelling-loader]");
    const sequenceTrack = document.querySelector(".scrollytelling-sequence__track");
    const hero = document.querySelector(".scrollytelling-hero");
    const totalFrames = 240;

    initScrollTopButton();
    animateFooter(footer);
    animateHero(hero);

    if (!canvas || !loader || !sequenceTrack) {
        return;
    }

    const ctx = canvas.getContext("2d", { alpha: false });
    const overlayKeys = ["intro", "stories", "brand", "cta"];
    const overlays = overlayKeys.reduce((accumulator, key) => {
        accumulator[key] = document.querySelector(`[data-overlay="${key}"]`);
        return accumulator;
    }, {});

    const sequenceState = {
        currentFrame: 0,
        targetFrame: 0,
        lastDrawnFrame: -1,
        rafId: 0,
        progress: 0
    };

    const frameSources = Array.from({ length: totalFrames }, (_, index) => {
        return `/frames/ezgif-frame-${String(index + 1).padStart(3, "0")}.jpg`;
    });

    document.body.classList.add("scrollytelling-is-loading");
    updateLoader(0);

    preloadFrames(frameSources, updateLoader)
        .then((images) => {
            const hydratedFrames = hydrateMissingFrames(images);
            if (!hydratedFrames[0] || !ctx) {
                throw new Error("Frame sequence could not be prepared.");
            }

            resizeCanvas(canvas);
            requestFrameDraw(hydratedFrames, sequenceState, ctx, canvas, 0, true);
            updateOverlayState(overlays, 0);
            initSequence(hydratedFrames, sequenceState, ctx, canvas, sequenceTrack, overlays);

            loader.classList.add("is-hidden");
            document.body.classList.remove("scrollytelling-is-loading");

            const redraw = () => {
                resizeCanvas(canvas);
                requestFrameDraw(hydratedFrames, sequenceState, ctx, canvas, sequenceState.currentFrame, true);
                if (typeof ScrollTrigger !== "undefined") {
                    ScrollTrigger.refresh();
                }
            };

            window.addEventListener("resize", redraw, { passive: true });
        })
        .catch((error) => {
            console.error("Scrollytelling preload failed.", error);
            document.body.classList.remove("scrollytelling-is-loading");
            loader.classList.add("is-hidden");
        });
});

function initScrollTopButton() {
    const scrollButton = document.createElement("button");
    scrollButton.type = "button";
    scrollButton.className = "scroll-top-button";
    scrollButton.setAttribute("aria-label", "Scroll to top");
    scrollButton.innerHTML = '<i class="fa-solid fa-arrow-up"></i>';
    document.body.appendChild(scrollButton);

    scrollButton.addEventListener("click", () => {
        window.scrollTo({ top: 0, behavior: "smooth" });
    });

    const updateScrollButton = () => {
        scrollButton.classList.toggle("is-visible", window.scrollY > 640);
    };

    updateScrollButton();
    window.addEventListener("scroll", updateScrollButton, { passive: true });
}

function animateHero(hero) {
    if (!hero || typeof gsap === "undefined") {
        return;
    }

    const title = hero.querySelector(".scrollytelling-hero__title");
    const copy = hero.querySelector("p:not(.scrollytelling-kicker)");
    const kicker = hero.querySelector(".scrollytelling-kicker");
    const arrow = hero.querySelector(".scrollytelling-arrow");
    const leftGlow = hero.querySelector(".scrollytelling-hero__glow--left");
    const rightGlow = hero.querySelector(".scrollytelling-hero__glow--right");

    gsap.from([kicker, title, copy, arrow].filter(Boolean), {
        y: 32,
        opacity: 0,
        stagger: 0.08,
        duration: 0.9,
        ease: "power3.out"
    });

    if (typeof ScrollTrigger === "undefined") {
        return;
    }

    gsap.registerPlugin(ScrollTrigger);

    if (leftGlow) {
        gsap.to(leftGlow, {
            x: 42,
            y: -28,
            ease: "none",
            scrollTrigger: {
                trigger: hero,
                start: "top top",
                end: "bottom top",
                scrub: true
            }
        });
    }

    if (rightGlow) {
        gsap.to(rightGlow, {
            x: -34,
            y: 46,
            ease: "none",
            scrollTrigger: {
                trigger: hero,
                start: "top top",
                end: "bottom top",
                scrub: true
            }
        });
    }
}

function animateFooter(footer) {
    if (!footer || typeof gsap === "undefined" || typeof ScrollTrigger === "undefined") {
        return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const footerCopy = footer.querySelector(".footer-copy");
    const footerActions = footer.querySelector(".footer-actions");

    if (footerCopy) {
        gsap.from(footerCopy, {
            y: 48,
            opacity: 0,
            duration: 0.9,
            ease: "power3.out",
            scrollTrigger: {
                trigger: footer,
                start: "top 82%"
            }
        });
    }

    if (footerActions) {
        gsap.from(footerActions, {
            y: 36,
            opacity: 0,
            duration: 0.9,
            delay: 0.08,
            ease: "power3.out",
            scrollTrigger: {
                trigger: footer,
                start: "top 82%"
            }
        });
    }
}

function preloadFrames(frameSources, onProgress) {
    let loadedCount = 0;

    const loaders = frameSources.map((src) => {
        return new Promise((resolve) => {
            const image = new Image();
            image.decoding = "async";
            image.onload = () => {
                loadedCount += 1;
                onProgress(Math.round((loadedCount / frameSources.length) * 100));
                resolve(image);
            };
            image.onerror = () => {
                loadedCount += 1;
                onProgress(Math.round((loadedCount / frameSources.length) * 100));
                resolve(null);
            };
            image.src = src;
        });
    });

    return Promise.all(loaders);
}

function hydrateMissingFrames(images) {
    const hydrated = [...images];
    let fallback = null;

    for (let index = 0; index < hydrated.length; index += 1) {
        if (hydrated[index]) {
            fallback = hydrated[index];
        } else if (fallback) {
            hydrated[index] = fallback;
        }
    }

    fallback = null;
    for (let index = hydrated.length - 1; index >= 0; index -= 1) {
        if (hydrated[index]) {
            fallback = hydrated[index];
        } else if (fallback) {
            hydrated[index] = fallback;
        }
    }

    return hydrated;
}

function resizeCanvas(canvas) {
    const pixelRatio = Math.min(window.devicePixelRatio || 1, 2);
    const { width, height } = canvas.getBoundingClientRect();
    const nextWidth = Math.max(1, Math.floor(width * pixelRatio));
    const nextHeight = Math.max(1, Math.floor(height * pixelRatio));

    if (canvas.width !== nextWidth || canvas.height !== nextHeight) {
        canvas.width = nextWidth;
        canvas.height = nextHeight;
    }
}

function requestFrameDraw(images, sequenceState, ctx, canvas, frameValue, force = false) {
    sequenceState.targetFrame = frameValue;

    if (sequenceState.rafId && !force) {
        return;
    }

    sequenceState.rafId = window.requestAnimationFrame(() => {
        sequenceState.rafId = 0;
        const frameIndex = Math.max(0, Math.min(images.length - 1, Math.round(sequenceState.targetFrame)));
        if (!force && sequenceState.lastDrawnFrame === frameIndex) {
            return;
        }

        sequenceState.currentFrame = frameIndex;
        sequenceState.lastDrawnFrame = frameIndex;
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(images[frameIndex], 0, 0, canvas.width, canvas.height);
    });
}

function initSequence(images, sequenceState, ctx, canvas, sequenceTrack, overlays) {
    if (typeof gsap === "undefined" || typeof ScrollTrigger === "undefined") {
        return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const playhead = { frame: 0 };

    gsap.to(playhead, {
        frame: images.length - 1,
        ease: "none",
        onUpdate: () => {
            requestFrameDraw(images, sequenceState, ctx, canvas, playhead.frame);
        },
        scrollTrigger: {
            trigger: sequenceTrack,
            start: "top top",
            end: "bottom bottom",
            scrub: 0.12,
            invalidateOnRefresh: true,
            onUpdate: (self) => {
                sequenceState.progress = self.progress;
                updateOverlayState(overlays, self.progress);
            }
        }
    });
}

function updateOverlayState(overlays, progress) {
    const activeKey = resolveActiveOverlay(progress);
    const parallaxMap = {
        intro: 0.15,
        stories: 0.45,
        brand: 0.75,
        cta: 0.95
    };

    Object.entries(overlays).forEach(([key, element]) => {
        if (!element) {
            return;
        }

        const isActive = key === activeKey;
        element.classList.toggle("is-active", isActive);

        if (!isActive) {
            element.style.removeProperty("transform");
            return;
        }

        const anchor = parallaxMap[key];
        const offset = Math.max(-18, Math.min(18, (anchor - progress) * 48));
        element.style.transform = `translate3d(0, ${offset}px, 0)`;
    });
}

function resolveActiveOverlay(progress) {
    if (progress <= 0.3) {
        return "intro";
    }

    if (progress <= 0.6) {
        return "stories";
    }

    if (progress <= 0.9) {
        return "brand";
    }

    return "cta";
}

function updateLoader(progress) {
    const loader = document.querySelector("[data-scrollytelling-loader]");
    const progressBar = document.querySelector("[data-scrollytelling-progress-bar]");
    const progressLabel = document.querySelector("[data-scrollytelling-progress-label]");

    if (loader) {
        loader.style.setProperty("--scrollytelling-progress", `${progress}%`);
    }

    if (progressBar) {
        progressBar.style.width = `${progress}%`;
    }

    if (progressLabel) {
        progressLabel.textContent = `${progress}%`;
    }
}
