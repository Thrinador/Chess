/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

/**
 *
 * @author Ben Clark
 */
public class CharacterHolder {

    private char value;

    public CharacterHolder() {
        value = ' ';
    }
    
    public CharacterHolder(char val) {
        value = val;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char val) {
        value = val;
    }
}
