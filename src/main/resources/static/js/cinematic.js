document.addEventListener("DOMContentLoaded", () => {
    const footer = document.querySelector(".site-footer");
    const canvas = document.querySelector("[data-scrollytelling-canvas]");
    const loader = document.querySelector("[data-scrollytelling-loader]");
    const sequenceTrack = document.querySelector(".scrollytelling-sequence__track");
    const hero = document.querySelector(".scrollytelling-hero");
    const totalFrames = 240;
    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    initScrollTopButton();
    animateFooter(footer, prefersReducedMotion);
    animateHero(hero, prefersReducedMotion);
    initKnowledgeConstellations(prefersReducedMotion);
    initLandingSectionReveals(prefersReducedMotion);

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
            initScrollytellingEnhancements(sequenceTrack, canvas, overlays, prefersReducedMotion);

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

function animateHero(hero, prefersReducedMotion = false) {
    if (!hero || typeof gsap === "undefined" || prefersReducedMotion) {
        return;
    }

    const title = hero.querySelector(".scrollytelling-hero__title");
    const copy = hero.querySelector("p:not(.scrollytelling-kicker)");
    const kicker = hero.querySelector(".scrollytelling-kicker");
    const arrow = hero.querySelector(".scrollytelling-arrow");
    const mesh = hero.querySelector(".scrollytelling-hero__mesh");
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

    if (mesh) {
        gsap.to(mesh, {
            yPercent: -8,
            opacity: 0.18,
            ease: "none",
            scrollTrigger: {
                trigger: hero,
                start: "top top",
                end: "bottom top",
                scrub: 0.45
            }
        });
    }

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

function animateFooter(footer, prefersReducedMotion = false) {
    if (!footer || typeof gsap === "undefined" || typeof ScrollTrigger === "undefined" || prefersReducedMotion) {
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

function initScrollytellingEnhancements(sequenceTrack, canvas, overlays, prefersReducedMotion = false) {
    if (prefersReducedMotion || typeof gsap === "undefined" || typeof ScrollTrigger === "undefined") {
        Object.values(overlays).forEach((element) => {
            if (element) {
                element.style.opacity = "";
                element.style.transform = "";
                element.style.filter = "";
            }
        });
        return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const sticky = sequenceTrack.querySelector(".scrollytelling-sequence__sticky");
    const ambient = sequenceTrack.querySelector(".scrollytelling-ambient");
    const ambientWashes = gsap.utils.toArray(".scrollytelling-ambient__wash", sticky);
    const ambientGrain = sticky ? sticky.querySelector(".scrollytelling-ambient__grain") : null;
    const shadow = sticky ? sticky.querySelector(".scrollytelling-book-shadow") : null;

    gsap.to(canvas, {
        y: -10,
        rotation: 0.16,
        scale: 1.012,
        duration: 5.8,
        yoyo: true,
        repeat: -1,
        ease: "sine.inOut"
    });

    if (shadow) {
        gsap.to(shadow, {
            scaleX: 1.08,
            scaleY: 0.86,
            duration: 5.8,
            yoyo: true,
            repeat: -1,
            ease: "sine.inOut",
            transformOrigin: "50% 50%"
        });
    }

    const depthTimeline = gsap.timeline({
        scrollTrigger: {
            trigger: sequenceTrack,
            start: "top bottom",
            end: "bottom top",
            scrub: 0.65,
            invalidateOnRefresh: true
        }
    });

    if (ambient) {
        depthTimeline.to(ambient, { yPercent: -9, ease: "none" }, 0);
    }

    if (ambientWashes.length) {
        depthTimeline.to(ambientWashes, { xPercent: 8, yPercent: -5, opacity: 0.44, ease: "none" }, 0);
    }

    if (ambientGrain) {
        depthTimeline.to(ambientGrain, { yPercent: -14, xPercent: 3, ease: "none" }, 0);
    }

    depthTimeline.to(canvas, { yPercent: -2.4, filter: "contrast(1.04) saturate(1.04)", ease: "none" }, 0);

    if (shadow) {
        depthTimeline.to(shadow, { yPercent: 12, opacity: 0.2, ease: "none" }, 0);
    }

    Object.values(overlays).forEach((overlay) => {
        if (!overlay) {
            return;
        }

        const children = gsap.utils.toArray(overlay.children);
        gsap.set(children, { y: 18, opacity: 0, filter: "blur(8px)" });
    });

    playOverlayReveal(resolveActiveOverlay(0), overlays);
}

function initLandingSectionReveals(prefersReducedMotion = false) {
    if (prefersReducedMotion || typeof gsap === "undefined" || typeof ScrollTrigger === "undefined") {
        return;
    }

    gsap.registerPlugin(ScrollTrigger);

    const sections = gsap.utils.toArray("main > div > section:not(.scrollytelling-hero):not(.scrollytelling-sequence):not(.scrollytelling-divider)");

    sections.forEach((section) => {
        section.classList.add("scrollytelling-reveal");
        gsap.fromTo(section,
            { opacity: 0.88, y: 34 },
            {
                opacity: 1,
                y: 0,
                duration: 0.9,
                ease: "power3.out",
                scrollTrigger: {
                    trigger: section,
                    start: "top 88%",
                    once: true
                }
            }
        );
    });
}

function initKnowledgeConstellations(prefersReducedMotion = false) {
    const canvases = Array.from(document.querySelectorAll("[data-constellation-canvas]"));
    if (prefersReducedMotion || !canvases.length) {
        return;
    }

    canvases.forEach((canvas, index) => {
        createKnowledgeConstellation(canvas, {
            variant: canvas.dataset.constellationVariant || "sequence",
            seed: 8300 + index * 97
        });
    });
}

function createKnowledgeConstellation(canvas, options) {
    const ctx = canvas.getContext("2d", { alpha: true });
    if (!ctx) {
        return;
    }

    const variant = options.variant === "hero" ? "hero" : "sequence";
    const settings = variant === "hero"
        ? {
            count: 168,
            connectionDistance: 86,
            centerX: 0.58,
            centerY: 0.48,
            radius: 0.36,
            spin: 0.000072,
            opacity: 0.92
        }
        : {
            count: 108,
            connectionDistance: 74,
            centerX: 0.52,
            centerY: 0.46,
            radius: 0.52,
            spin: 0.000045,
            opacity: 0.56
        };

    const random = seededRandom(options.seed);
    const points = Array.from({ length: settings.count }, (_, index) => {
        const u = random() * 2 - 1;
        const theta = random() * Math.PI * 2;
        const radialBias = variant === "hero" ? 0.78 + random() * 0.22 : 0.34 + random() * 0.66;
        const ring = Math.sqrt(1 - u * u) * radialBias;

        return {
            x: Math.cos(theta) * ring,
            y: u * radialBias,
            z: Math.sin(theta) * ring,
            size: 0.55 + random() * (variant === "hero" ? 1.15 : 0.85),
            alpha: 0.18 + random() * 0.62,
            phase: random() * Math.PI * 2,
            tone: index % 31 === 0 ? "gold" : index % 19 === 0 ? "violet" : index % 13 === 0 ? "cyan" : "paper"
        };
    });

    let width = 0;
    let height = 0;
    let rafId = 0;
    let isVisible = true;

    const resize = () => {
        const rect = canvas.getBoundingClientRect();
        const pixelRatio = Math.min(window.devicePixelRatio || 1, 1.6);
        const nextWidth = Math.max(1, Math.floor(rect.width * pixelRatio));
        const nextHeight = Math.max(1, Math.floor(rect.height * pixelRatio));

        if (canvas.width !== nextWidth || canvas.height !== nextHeight) {
            canvas.width = nextWidth;
            canvas.height = nextHeight;
        }

        width = nextWidth;
        height = nextHeight;
        ctx.setTransform(1, 0, 0, 1, 0, 0);
    };

    const draw = (timestamp) => {
        rafId = 0;
        if (!isVisible || document.hidden) {
            return;
        }

        ctx.clearRect(0, 0, width, height);
        ctx.globalCompositeOperation = "lighter";

        const projected = projectConstellationPoints(points, settings, width, height, timestamp);
        drawConstellationConnections(ctx, projected, settings.connectionDistance, settings.opacity);
        drawConstellationNodes(ctx, projected, settings.opacity, timestamp);

        rafId = window.requestAnimationFrame(draw);
    };

    const start = () => {
        if (!rafId) {
            rafId = window.requestAnimationFrame(draw);
        }
    };

    const stop = () => {
        if (rafId) {
            window.cancelAnimationFrame(rafId);
            rafId = 0;
        }
    };

    if ("IntersectionObserver" in window) {
        const observer = new IntersectionObserver((entries) => {
            isVisible = entries.some((entry) => entry.isIntersecting);
            if (isVisible && !document.hidden) {
                start();
            } else {
                stop();
            }
        }, { rootMargin: "160px" });
        observer.observe(canvas);
    }

    document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
            stop();
        } else if (isVisible) {
            resize();
            start();
        }
    });

    if ("ResizeObserver" in window) {
        const resizeObserver = new ResizeObserver(() => {
            resize();
        });
        resizeObserver.observe(canvas);
    } else {
        window.addEventListener("resize", resize, { passive: true });
    }

    resize();
    start();
}

function projectConstellationPoints(points, settings, width, height, timestamp) {
    const radius = Math.min(width, height) * settings.radius;
    const centerX = width * settings.centerX;
    const centerY = height * settings.centerY;
    const spin = timestamp * settings.spin;
    const cos = Math.cos(spin);
    const sin = Math.sin(spin);
    const focal = 2.4;

    return points.map((point) => {
        const drift = Math.sin(timestamp * 0.00038 + point.phase) * 0.035;
        const x = point.x * cos - point.z * sin;
        const z = point.x * sin + point.z * cos;
        const y = point.y + drift;
        const scale = focal / (focal + z);

        return {
            x: centerX + x * radius * scale,
            y: centerY + y * radius * scale,
            scale,
            size: point.size,
            alpha: point.alpha * (0.6 + scale * 0.34),
            phase: point.phase,
            tone: point.tone
        };
    });
}

function drawConstellationConnections(ctx, points, maxDistance, opacity) {
    ctx.lineWidth = 0.55;

    for (let i = 0; i < points.length; i += 1) {
        const point = points[i];
        let links = 0;

        for (let j = i + 1; j < points.length; j += 1) {
            const next = points[j];
            const dx = point.x - next.x;
            const dy = point.y - next.y;
            const distance = Math.hypot(dx, dy);

            if (distance < maxDistance && links < 3) {
                const alpha = (1 - distance / maxDistance) * 0.2 * opacity;
                ctx.strokeStyle = `rgba(101, 207, 201, ${alpha})`;
                ctx.beginPath();
                ctx.moveTo(point.x, point.y);
                ctx.lineTo(next.x, next.y);
                ctx.stroke();
                links += 1;
            }
        }
    }
}

function drawConstellationNodes(ctx, points, opacity, timestamp) {
    points.forEach((point) => {
        const pulse = 0.68 + Math.sin(timestamp * 0.0011 + point.phase) * 0.32;
        const color = resolveConstellationColor(point.tone);
        const alpha = Math.max(0.05, point.alpha * opacity * pulse);
        const radius = point.size * point.scale;

        if (point.tone !== "paper") {
            const glow = ctx.createRadialGradient(point.x, point.y, 0, point.x, point.y, radius * 8);
            glow.addColorStop(0, color.glow);
            glow.addColorStop(1, "rgba(0, 0, 0, 0)");
            ctx.fillStyle = glow;
            ctx.beginPath();
            ctx.arc(point.x, point.y, radius * 8, 0, Math.PI * 2);
            ctx.fill();
        }

        ctx.fillStyle = `rgba(${color.rgb}, ${alpha})`;
        ctx.beginPath();
        ctx.arc(point.x, point.y, Math.max(0.7, radius), 0, Math.PI * 2);
        ctx.fill();
    });
}

function resolveConstellationColor(tone) {
    if (tone === "cyan") {
        return { rgb: "83, 215, 208", glow: "rgba(83, 215, 208, 0.34)" };
    }

    if (tone === "violet") {
        return { rgb: "159, 96, 255", glow: "rgba(159, 96, 255, 0.3)" };
    }

    if (tone === "gold") {
        return { rgb: "214, 170, 103", glow: "rgba(214, 170, 103, 0.34)" };
    }

    return { rgb: "226, 236, 226", glow: "rgba(226, 236, 226, 0.16)" };
}

function seededRandom(seed) {
    let state = seed % 2147483647;
    if (state <= 0) {
        state += 2147483646;
    }

    return () => {
        state = state * 16807 % 2147483647;
        return (state - 1) / 2147483646;
    };
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
        const wasActive = element.classList.contains("is-active");
        element.classList.toggle("is-active", isActive);

        if (!isActive) {
            element.style.removeProperty("transform");
            return;
        }

        const anchor = parallaxMap[key];
        const offset = Math.max(-18, Math.min(18, (anchor - progress) * 48));
        element.style.transform = `translate3d(0, ${offset}px, 0)`;

        if (!wasActive) {
            playOverlayReveal(key, overlays);
        }
    });
}

function playOverlayReveal(activeKey, overlays) {
    if (typeof gsap === "undefined") {
        return;
    }

    const overlay = overlays[activeKey];
    if (!overlay || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return;
    }

    gsap.killTweensOf(overlay.children);
    gsap.fromTo(overlay.children,
        { y: 18, opacity: 0, filter: "blur(8px)" },
        {
            y: 0,
            opacity: 1,
            filter: "blur(0px)",
            duration: 0.72,
            stagger: 0.075,
            ease: "power3.out",
            overwrite: "auto"
        }
    );
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
