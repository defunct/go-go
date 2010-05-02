package com.goodworkalan.go.go;

import com.goodworkalan.infuse.ObjectInfuser;

/**
 * Assigns a command line argument to a property in a {@link Arguable} Bean.
 * 
 * @author Alan Gutierrez
 */
class Assignment {
    /** The method to set the task property. */
    public final Setter setter;

    /** The converter for the string property. */
    public final ObjectInfuser infuser;

    /**
     * Create an assignment that will set the property identified by the given
     * setter method with a string value converted with the given converter.
     * 
     * @param declaringClass
     *            The class that declared the assignment.
     * @param setter
     *            The property setter.
     * @param infuser
     *            The string converter.
     */
    public Assignment(Setter setter, ObjectInfuser infuser) {
        this.setter = setter;
        this.infuser = infuser;
    }

    /**
     * Get the assignment type.
     * 
     * @return The assignment type.
     */
    public Class<?> getType() {
        return setter.getType();
    }
}
