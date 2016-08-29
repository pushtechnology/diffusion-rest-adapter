
'use strict';

var gulp = require('gulp'),
    jasmine = require('gulp-jasmine'),
    ts = require('gulp-typescript'),
    tsdoc = require('gulp-typedoc'),
    tslint = require('gulp-tslint'),
    merge = require('merge-stream'),
    browserify = require('browserify'),
    globby = require('globby'),
    source = require('vinyl-source-stream'),
    rename = require('gulp-rename'),
    uglify = require('gulp-uglify'),
    typings = require('gulp-typings'),
    buffer = require('vinyl-buffer');

gulp.task('install-typings', function(done) {
    return gulp
        .src('typings.json')
        .pipe(gulp.dest('target'))
        .pipe(typings());
});

gulp.task('generate-javascript', ['install-typings'], function() {
    var tsResult = gulp.src(['src/main/ts/**/*.ts', 'target/typings/**/*.d.ts'])
        .pipe(ts({
            target : 'ES5',
            module : 'commonjs',
            moduleResolution : 'node',
            declaration : true,
            emitDecoratorMetadata: true,
            experimentalDecorators : true
        }));

    // Pipe the generated JavaScript to the build directory
    tsResult.js
        .pipe(gulp.dest('target/js'));

    // Return the result of generating the JavaScript to fail the build if
    // there are any TypeScript errors
    return tsResult;
});

gulp.task('generate-dist', ['generate-javascript'], function(done) {
    // Package both the source JavaScript and the generated JavaScript using
    // browserify
    globby(['target/js/**/*.js', 'src/main/js/**/*.js']).then(function(entries) {
        browserify({
            entries: entries,
            debug: false,
            paths: ['target/js']
        })
        .transform('browserify-shim')
        .bundle()
        .pipe(source('index.js'))
        .pipe(rename('client.js'))
        .pipe(buffer())
        .pipe(uglify({
            mangle: false
        }))
        .pipe(gulp.dest('target/dist/js'))
        .on('end', function() {
            done();
        })
        .on('error', function (error) {
            done(error);
        });
    }, function (error) {
        done(error);
    });
});

gulp.task('checks', function() {
    return gulp.src('src/main/ts/*.ts')
        .pipe(tslint({
            configuration : {
                rules : {
                    'class-name' : true,
                    'no-consecutive-blank-lines' : true
                }
            }
        }))
        .pipe(tslint.report('verbose'));
});

gulp.task('doc', function() {
    return gulp.src(['src/main/ts/*.ts'])
        .pipe(tsdoc({
            target : 'ES5',
            module : 'commonjs',
            moduleResolution : 'node',
            mode : 'file',
            excludeNotExported : true,
            excludeExternals : true,
            out : 'target/doc',
        }));
});

gulp.task('default', ['install-typings', 'generate-javascript', 'generate-dist', 'checks']);
