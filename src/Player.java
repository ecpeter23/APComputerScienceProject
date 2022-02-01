public class Player {
    int x;
    int y;
    int health;
    int food;
    int armor;
    int money;
    int skillPoints;
    int[] inventory;
    String[] skills;
    int[] skillLevels;
    int equipedItem;

    public Player(){
        x = 0;
        y = 0;
        health = 100;
        food = 100;
        armor = 0;
        money = 100;
        skillPoints = 10;
        inventory = new int[15];
        skills = new String[]{"Intelligence", "Charisma", "Luck", "Fitness", "Unarmed", "One Handed", "Rifles", "Pistols", "Explosives", "Lockpicking", "Medicine", "Sneak"};
        skillLevels = new int[12];
        equipedItem = 0;
    }
}
