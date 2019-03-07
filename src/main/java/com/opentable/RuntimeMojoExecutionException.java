package com.opentable;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * A runtime exception wrapper around a MojoExecutionException
 */
public class RuntimeMojoExecutionException extends RuntimeException {

    private static final long serialVersionUID = 4612583331909658256L;

    private final MojoExecutionException mojoExecutionException;

    public RuntimeMojoExecutionException(MojoExecutionException mojoExecutionException) {
        this.mojoExecutionException = mojoExecutionException;
    }

    public MojoExecutionException getMojoExecutionException() {
        return mojoExecutionException;
    }

}
